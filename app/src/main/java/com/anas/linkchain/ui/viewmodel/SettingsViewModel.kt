package com.anas.linkchain.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anas.linkchain.LinkChainApp
import com.anas.linkchain.data.security.CryptoManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = (app as LinkChainApp).preferencesManager

    val isDarkTheme = prefs.isDarkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val fixedQuality = prefs.fixedQuality.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1080p")
    val wifiOnly = prefs.wifiOnly.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val skipDuplicates = prefs.skipDuplicates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val acceptSharedLinks = prefs.acceptSharedLinks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val appLockEnabled = prefs.appLockEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val lowStorageWarn = prefs.lowStorageWarn.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val targetDownloaderPkg = prefs.targetDownloaderPackage.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val downloadFolderUri = prefs.downloadFolderUri.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch { prefs.setDarkTheme(enabled) }
    fun setFixedQuality(q: String) = viewModelScope.launch { prefs.setFixedQuality(q) }
    fun setWifiOnly(enabled: Boolean) = viewModelScope.launch { prefs.setWifiOnly(enabled) }
    fun setSkipDuplicates(enabled: Boolean) = viewModelScope.launch { prefs.setSkipDuplicates(enabled) }
    fun setAcceptSharedLinks(enabled: Boolean) = viewModelScope.launch { prefs.setAcceptSharedLinks(enabled) }
    fun setLowStorageWarn(enabled: Boolean) = viewModelScope.launch { prefs.setLowStorageWarn(enabled) }
    fun setTargetDownloaderPkg(pkg: String) = viewModelScope.launch { prefs.setTargetDownloaderPackage(pkg) }
    fun setDownloadFolderUri(uri: String) = viewModelScope.launch { prefs.setDownloadFolderUri(uri) }

    fun setPin(pin: String) = viewModelScope.launch {
        val hash = CryptoManager.hashPin(pin)
        prefs.setPinHash(hash)
        prefs.setAppLockEnabled(true)
    }

    fun disableAppLock() = viewModelScope.launch {
        prefs.setAppLockEnabled(false)
        prefs.setPinHash("")
    }
}