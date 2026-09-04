package com.cliply.core.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.cliplyDataStore by preferencesDataStore("cliply_settings")
data class CliplySettings(val downloadQuality: String = "Best available", val saveLocation: String = "Device", val wifiOnly: Boolean = false, val simultaneousDownloads: Int = 2, val downloadNotifications: Boolean = true, val notificationSound: Boolean = true, val notificationVibration: Boolean = false, val autoDetectLinks: Boolean = true, val theme: String = "Dark", val language: String = "English")
class SettingsRepository(private val context: Context) {
    private object Keys { val quality = stringPreferencesKey("download_quality"); val location = stringPreferencesKey("save_location"); val wifi = booleanPreferencesKey("wifi_only"); val simultaneous = intPreferencesKey("simultaneous_downloads"); val notifications = booleanPreferencesKey("download_notifications"); val sound = booleanPreferencesKey("notification_sound"); val vibration = booleanPreferencesKey("notification_vibration"); val autodetect = booleanPreferencesKey("auto_detect_links"); val theme = stringPreferencesKey("theme"); val language = stringPreferencesKey("language") }
    val settings: Flow<CliplySettings> = context.cliplyDataStore.data.map { p -> CliplySettings(p[Keys.quality] ?: "Best available", p[Keys.location] ?: "Device", p[Keys.wifi] ?: false, p[Keys.simultaneous] ?: 2, p[Keys.notifications] ?: true, p[Keys.sound] ?: true, p[Keys.vibration] ?: false, p[Keys.autodetect] ?: true, p[Keys.theme] ?: "Dark", p[Keys.language] ?: "English") }
    suspend fun setWifiOnly(value: Boolean) { context.cliplyDataStore.edit { it[Keys.wifi] = value } }
}
