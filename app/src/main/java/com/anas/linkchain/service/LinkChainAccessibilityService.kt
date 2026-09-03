package com.anas.linkchain.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class LinkChainAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    fun performPasteAndDownload(url: String, quality: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val inputNodes = rootNode.findAccessibilityNodeInfosByViewId(TargetDownloaderConfig.INPUT_FIELD_ID)
        if (inputNodes.isNotEmpty()) {
            val input = inputNodes[0]
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, url)
            }
            input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        val downloadButtons = rootNode.findAccessibilityNodeInfosByViewId(TargetDownloaderConfig.CONFIRM_DOWNLOAD_BUTTON_ID)
        if (downloadButtons.isNotEmpty()) {
            downloadButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }

        return false
    }

    override fun onInterrupt() {
        isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    companion object {
        var isRunning: Boolean = false
            private set
    }
}