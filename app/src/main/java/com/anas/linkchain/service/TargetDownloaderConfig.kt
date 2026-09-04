package com.anas.linkchain.service

object TargetDownloaderConfig {

    // ── Package Identity ──────────────────────────────────────────────────────
    const val DEFAULT_PACKAGE = "video.downloader.videodownloader"

    // ── View IDs  ─────────────────────────────────────────────────────────────
    // These are the best-known resource IDs for video.downloader.videodownloader.
    // If a future app update renames them, the TEXT / CONTENT-DESC fallbacks
    // below will still work automatically.
    const val INPUT_FIELD_ID            = "video.downloader.videodownloader:id/et_url"
    const val QUALITY_DIALOG_BUTTON_ID  = "video.downloader.videodownloader:id/btn_quality"
    const val CONFIRM_DOWNLOAD_BUTTON_ID = "video.downloader.videodownloader:id/btn_download"

    // ── Download Button Text Fallbacks ────────────────────────────────────────
    // Tried in order when the view ID above is not found.
    // Covers different app versions, locales, and UI variations.
    val DOWNLOAD_BUTTON_TEXTS = listOf(
        "Download",
        "DOWNLOAD",
        "Download Video",
        "Download video",
        "Save",
        "SAVE"
    )

    // ── Download Button Content-Description Fallbacks ─────────────────────────
    val DOWNLOAD_CONTENT_DESCRIPTIONS = listOf(
        "Download",
        "Download video",
        "Download button",
        "download_btn",
        "Save video"
    )

    // ── Notification Completion Phrases ───────────────────────────────────────
    // Matched (case-insensitive) against notification title+text combined.
    // InShot Video Downloader posts one of these when a download finishes.
    val NOTIF_COMPLETION_PHRASES = listOf(
        "download complete",
        "download completed",
        "downloaded",
        "download successful",
        "download finished",
        "saved"
    )

    // Legacy single-phrase kept for compatibility with existing code that reads it.
    const val NOTIF_COMPLETION_PHRASE = "download complete"
}
