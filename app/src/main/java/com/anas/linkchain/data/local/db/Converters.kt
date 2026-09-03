package com.anas.linkchain.data.local.db

import androidx.room.TypeConverter
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.domain.model.PlatformType

class Converters {
    @TypeConverter
    fun fromStatus(status: ItemStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): ItemStatus = runCatching {
        ItemStatus.valueOf(value)
    }.getOrDefault(ItemStatus.PENDING)

    @TypeConverter
    fun fromPlatform(platform: PlatformType): String = platform.name

    @TypeConverter
    fun toPlatform(value: String): PlatformType = runCatching {
        PlatformType.valueOf(value)
    }.getOrDefault(PlatformType.GENERIC)
}