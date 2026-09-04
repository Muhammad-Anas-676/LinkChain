package com.anas.linkchain.service

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class LinkChainAccessibilityService : AccessibilityService() {

    private var lastHandledUrl: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
    }

    // ── Main event handler ────────────────────────────────────────────────────

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val url = pendingUrl ?: return
        val pkg = event?.packageName?.toString() ?: return

        if (pkg != currentTargetPackage) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        if (url == lastHandledUrl) return

        val rootNode = rootInActiveWindow ?: return

        // ── Step 1: Fill URL input if the target app has one ─────────────────
        // InShot Video Downloader uses a built-in browser — the URL is delivered
        // via the share intent and loaded automatically, so this step is skipped
        // when the field is absent. For URL-input style apps it still works.
        val inputNodes = rootNode.findAccessibilityNodeInfosByViewId(
            TargetDownloaderConfig.INPUT_FIELD_ID
        )
        if (inputNodes.isNotEmpty()) {
            val input = inputNodes[0]
            if (input.text?.toString() != url) {
                val args = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        url
                    )
                }
                input.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                return // wait for next event after text is committed
            }
        }

        // ── Step 2: Click Download button — three strategies in priority order ──

        // Strategy A: known view ID
        if (clickByViewId(rootNode, TargetDownloaderConfig.CONFIRM_DOWNLOAD_BUTTON_ID)) {
            markHandled(url); return
        }

        // Strategy B: button text matching
        for (text in TargetDownloaderConfig.DOWNLOAD_BUTTON_TEXTS) {
            if (clickByText(rootNode, text)) {
                markHandled(url); return
            }
        }

        // Strategy C: content-description matching
        for (desc in TargetDownloaderConfig.DOWNLOAD_CONTENT_DESCRIPTIONS) {
            if (clickByContentDescription(rootNode, desc)) {
                markHandled(url); return
            }
        }
    }

    // ── Strategy helpers ──────────────────────────────────────────────────────

    private fun clickByViewId(root: AccessibilityNodeInfo, viewId: String): Boolean {
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        val btn = nodes.firstOrNull { it.isEnabled && it.isClickable } ?: return false
        btn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return true
    }

    private fun clickByText(root: AccessibilityNodeInfo, text: String): Boolean {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        val btn = nodes.firstOrNull { it.isEnabled && it.isClickable } ?: return false
        btn.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        return true
    }

    private fun clickByContentDescription(
        root: AccessibilityNodeInfo,
        description: String
    ): Boolean {
        return walkAndClick(root, description)
    }

    /** DFS walk to find a clickable node whose contentDescription contains [desc]. */
    private fun walkAndClick(node: AccessibilityNodeInfo, desc: String): Boolean {
        val cd = node.contentDescription?.toString() ?: ""
        if (node.isClickable && node.isEnabled &&
            cd.contains(desc, ignoreCase = true)
        ) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (walkAndClick(child, desc)) return true
        }
        return false
    }

    private fun markHandled(url: String) {
        lastHandledUrl = url
        pendingUrl = null
    }

    // ── Manual call path (kept for backward-compat / debug) ──────────────────

    fun performPasteAndDownload(url: String, @Suppress("UNUSED_PARAMETER") quality: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false

        val inputNodes = rootNode.findAccessibilityNodeInfosByViewId(
            TargetDownloaderConfig.INPUT_FIELD_ID
        )
        if (inputNodes.isNotEmpty()) {
            val args = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    url
                )
            }
            inputNodes[0].performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }

        if (clickByViewId(rootNode, TargetDownloaderConfig.CONFIRM_DOWNLOAD_BUTTON_ID)) return true
        for (text in TargetDownloaderConfig.DOWNLOAD_BUTTON_TEXTS) {
            if (clickByText(rootNode, text)) return true
        }
        return false
    }

    override fun onInterrupt() { isRunning = false }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    companion object {
        var isRunning: Boolean = false
            private set

        @Volatile var pendingUrl: String? = null

        @Volatile var currentTargetPackage: String = TargetDownloaderConfig.DEFAULT_PACKAGE
    }
}
