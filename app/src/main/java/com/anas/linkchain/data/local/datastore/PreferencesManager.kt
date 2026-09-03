package com.anas.linkchain.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "linkchain_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FIXED_QUALITY = stringPreferencesKey("fixed_quality")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val SKIP_DUPLICATES = booleanPreferencesKey("skip_duplicates")
        val ACCEPT_SHARED_LINKS = booleanPreferencesKey("accept_shared_links")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val LOW_STORAGE_WARN = booleanPreferencesKey("low_storage_warn")
        val DOWNLOAD_FOLDER_URI = stringPreferencesKey("download_folder_uri")
        val TARGET_DOWNLOADER_PACKAGE = stringPreferencesKey("target_downloader_package")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[DARK_THEME] ?: true }
    val fixedQuality: Flow<String> = context.dataStore.data.map { it[FIXED_QUALITY] ?: "1080p" }
    val wifiOnly: Flow<Boolean> = context.dataStore.data.map { it[WIFI_ONLY] ?: false }
    val skipDuplicates: Flow<Boolean> = context.dataStore.data.map { it[SKIP_DUPLICATES] ?: true }
    val acceptSharedLinks: Flow<Boolean> = context.dataStore.data.map { it[ACCEPT_SHARED_LINKS] ?: true }
    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[APP_LOCK_ENABLED] ?: false }
    val pinHash: Flow<String> = context.dataStore.data.map { it[PIN_HASH] ?: "" }
    val targetDownloaderPackage: Flow<String> = context.dataStore.data.map { it[TARGET_DOWNLOADER_PACKAGE] ?: "" }
    val lowStorageWarn: Flow<Boolean> = context.dataStore.data.map { it[LOW_STORAGE_WARN] ?: true }
    val downloadFolderUri: Flow<String> = context.dataStore.data.map { it[DOWNLOAD_FOLDER_URI] ?: "" }

    suspend fun setDarkTheme(enabled: Boolean) = context.dataStore.edit { it[DARK_THEME] = enabled }
    suspend fun setFixedQuality(quality: String) = context.dataStore.edit { it[FIXED_QUALITY] = quality }
    suspend fun setWifiOnly(enabled: Boolean) = context.dataStore.edit { it[WIFI_ONLY] = enabled }
    suspend fun setSkipDuplicates(enabled: Boolean) = context.dataStore.edit { it[SKIP_DUPLICATES] = enabled }
    suspend fun setAcceptSharedLinks(enabled: Boolean) = context.dataStore.edit { it[ACCEPT_SHARED_LINKS] = enabled }
    suspend fun setAppLockEnabled(enabled: Boolean) = context.dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    suspend fun setPinHash(hash: String) = context.dataStore.edit { it[PIN_HASH] = hash }
    suspend fun setTargetDownloaderPackage(pkg: String) = context.dataStore.edit { it[TARGET_DOWNLOADER_PACKAGE] = pkg }
    suspend fun setLowStorageWarn(enabled: Boolean) = context.dataStore.edit { it[LOW_STORAGE_WARN] = enabled }
    suspend fun setDownloadFolderUri(uri: String) = context.dataStore.edit { it[DOWNLOAD_FOLDER_URI] = uri }
}