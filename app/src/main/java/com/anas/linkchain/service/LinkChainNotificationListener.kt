package com.anas.linkchain.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class LinkChainNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        isListenerConnected = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isListenerConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val pkg = sbn.packageName ?: return

        // Only watch notifications from the configured downloader package
        val targetPkg = LinkChainAccessibilityService.currentTargetPackage
        if (pkg != targetPkg) return

        val extras = notification.extras ?: return
        val title = extras.getString("android.title") ?: ""
        val text  = extras.getCharSequence("android.text")?.toString() ?: ""
        val combined = "$title $text".lowercase()

        // Match any of the known completion phrases
        val completed = TargetDownloaderConfig.NOTIF_COMPLETION_PHRASES
            .any { phrase -> combined.contains(phrase) }

        if (completed) {
            _completionEvents.tryEmit(pkg)
        }
    }

    companion object {
        var isListenerConnected: Boolean = false
            private set

        private val _completionEvents = MutableSharedFlow<String>(extraBufferCapacity = 64)
        val completionEvents: SharedFlow<String> = _completionEvents.asSharedFlow()
    }
}
