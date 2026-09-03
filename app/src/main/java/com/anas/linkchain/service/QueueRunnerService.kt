package com.anas.linkchain.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.anas.linkchain.LinkChainApp
import com.anas.linkchain.domain.model.HistoryItem
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.domain.model.QueueItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class QueueRunnerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification("Starting queue runner...", ""))
            startQueueLoop()
        }
        return START_NOT_STICKY
    }

    private fun startQueueLoop() {
        serviceScope.launch {
            val app = LinkChainApp.instance
            val queueDao = app.database.queueDao()
            val historyDao = app.database.historyDao()
            val prefs = app.preferencesManager

            var currentItem: QueueItem? = queueDao.getNextPending()

            while (currentItem != null && isRunning) {
                val item = currentItem

                val wifiOnly = prefs.wifiOnly.first()
                if (wifiOnly && !isWifiConnected()) {
                    updateNotification("Paused: Waiting for Wi-Fi", item.url)
                    delay(5000)
                    continue
                }

                queueDao.updateStatus(item.id, ItemStatus.DOWNLOADING)
                updateNotification("Downloading: ${item.platform.name}", item.url)

                val targetPkg = prefs.targetDownloaderPackage.first().ifBlank {
                    TargetDownloaderConfig.DEFAULT_PACKAGE
                }
                launchDownloader(targetPkg, item.url)

                val success = waitForCompletionOrTimeout(180_000L)

                if (success) {
                    historyDao.insert(
                        HistoryItem(
                            url = item.url,
                            status = ItemStatus.DONE,
                            quality = item.quality,
                            platform = item.platform
                        )
                    )
                    queueDao.deleteById(item.id)
                } else {
                    queueDao.updateStatus(item.id, ItemStatus.FAILED)
                }

                currentItem = queueDao.getNextPending()
            }

            val failedList = queueDao.getAllFailed()
            for (failed in failedList) {
                queueDao.updateStatus(failed.id, ItemStatus.DOWNLOADING)
                updateNotification("Retrying item...", failed.url)
                launchDownloader(TargetDownloaderConfig.DEFAULT_PACKAGE, failed.url)
                val success = waitForCompletionOrTimeout(180_000L)
                historyDao.insert(
                    HistoryItem(
                        url = failed.url,
                        status = if (success) ItemStatus.DONE else ItemStatus.FAILED,
                        quality = failed.quality,
                        platform = failed.platform
                    )
                )
                queueDao.deleteById(failed.id)
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun launchDownloader(packageName: String, url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(shareIntent)
        } catch (_: Exception) {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }
    }

    private suspend fun waitForCompletionOrTimeout(timeoutMs: Long): Boolean {
        return withTimeoutOrNull(timeoutMs) {
            LinkChainNotificationListener.completionEvents.first()
            true
        } ?: false
    }

    private fun isWifiConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun updateNotification(title: String, content: String) {
        val notif = createNotification(title, content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notif)
    }

    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, LinkChainApp.CHANNEL_QUEUE_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}