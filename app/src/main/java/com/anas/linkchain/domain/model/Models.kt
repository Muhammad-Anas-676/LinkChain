package com.anas.linkchain.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    DONE,
    FAILED
}

enum class PlatformType {
    YOUTUBE,
    INSTAGRAM,
    TIKTOK,
    GENERIC;

    companion object {
        fun fromUrl(url: String): PlatformType {
            val lower = url.lowercase()
            return when {
                lower.contains("youtube.com") || lower.contains("youtu.be") -> YOUTUBE
                lower.contains("instagram.com") -> INSTAGRAM
                lower.contains("tiktok.com") -> TIKTOK
                else -> GENERIC
            }
        }
    }
}

@Entity(tableName = "queue_items")
data class QueueItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val status: ItemStatus = ItemStatus.PENDING,
    val quality: String = "1080p",
    val platform: PlatformType = PlatformType.GENERIC,
    val createdAt: Long = System.currentTimeMillis(),
    val position: Int = 0
)

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val status: ItemStatus = ItemStatus.DONE,
    val quality: String = "1080p",
    val platform: PlatformType = PlatformType.GENERIC,
    val completedAt: Long = System.currentTimeMillis()
)