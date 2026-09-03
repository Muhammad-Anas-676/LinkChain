package com.anas.linkchain.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import android.webkit.URLUtil
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anas.linkchain.LinkChainApp
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.domain.model.PlatformType
import com.anas.linkchain.domain.model.QueueItem
import com.anas.linkchain.service.QueueRunnerService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QueueViewModel(app: Application) : AndroidViewModel(app) {

    private val db = (app as LinkChainApp).database
    private val queueDao = db.queueDao()
    private val historyDao = db.historyDao()
    private val prefs = app.preferencesManager

    val queueItems = queueDao.getAllItems().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val fixedQuality = prefs.fixedQuality.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1080p")
    val lowStorageWarnSetting = prefs.lowStorageWarn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _storageLow = MutableStateFlow(false)
    val storageLow: StateFlow<Boolean> = _storageLow.asStateFlow()

    init {
        checkStorageSpace()
    }

    fun checkStorageSpace() {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        val availableMb = availableBytes / (1024 * 1024)
        _storageLow.value = availableMb < 500
    }

    fun dismissStorageWarning() {
        _storageLow.value = false
    }

    fun addLinksToQueue(urls: List<String>, onResult: (added: Int, skipped: Int) -> Unit) {
        viewModelScope.launch {
            val skipDup = prefs.skipDuplicates.first()
            val quality = fixedQuality.value
            var addedCount = 0
            var skippedCount = 0

            val validUrls = urls.map { it.trim() }.filter { URLUtil.isValidUrl(it) }

            for (url in validUrls) {
                if (skipDup) {
                    val inQueue = queueDao.findByUrl(url) != null
                    val inHistory = historyDao.findByUrl(url) != null
                    if (inQueue || inHistory) {
                        skippedCount++
                        continue
                    }
                }
                queueDao.insert(
                    QueueItem(
                        url = url,
                        quality = quality,
                        platform = PlatformType.fromUrl(url),
                        status = ItemStatus.PENDING
                    )
                )
                addedCount++
            }
            onResult(addedCount, skippedCount)
        }
    }

    fun togglePause(item: QueueItem) {
        viewModelScope.launch {
            val newStatus = if (item.status == ItemStatus.PAUSED) ItemStatus.PENDING else ItemStatus.PAUSED
            queueDao.updateStatus(item.id, newStatus)
        }
    }

    fun startBatch() {
        val context = getApplication<Application>()
        val intent = Intent(context, QueueRunnerService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}