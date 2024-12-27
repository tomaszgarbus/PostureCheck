package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore("Settings")
class SettingsRepository(private val context: Context) {
    private val notificationsPerDayKey: Preferences.Key<Int> = intPreferencesKey("notifications_per_day")
    private val earliestNotificationTimeKey: Preferences.Key<Int> = intPreferencesKey("earliest_notification_time")
    private val latestNotificationTimeKey: Preferences.Key<Int> = intPreferencesKey("latest_notification_time")

    fun getNotificationsPerDayAsFlow(): Flow<Int> {
        return context.settingsDataStore.data.map {
            preferences -> preferences[notificationsPerDayKey] ?: DefaultSettings.defaulNotificationsPerDay
        }
    }

    suspend fun setNotificationsPerDay(
        notificationsPerDay: Int) {
        context.settingsDataStore.edit { mutablePreferences ->
            mutablePreferences[notificationsPerDayKey] = notificationsPerDay
        }
    }

    fun getEarliestNotificationTimeAsFlow(): Flow<TimeOfDay> {
        return context.settingsDataStore.data.map {
            preferences -> TimeOfDay.fromPreferencesStorageFormat(
            preferences[earliestNotificationTimeKey] ?: DefaultSettings.defaultEarliestNotificationTime.toPreferencesStorageFormat())
        }
    }

    suspend fun setEarliestNotificationTime(time: TimeOfDay) {
        context.settingsDataStore.edit { mutablePreferences ->
            mutablePreferences[earliestNotificationTimeKey] = time.toPreferencesStorageFormat()
        }
    }

    fun getLatestNotificationTimeAsFlow(): Flow<TimeOfDay> {
        return context.settingsDataStore.data.map {
                preferences -> TimeOfDay.fromPreferencesStorageFormat(
            preferences[latestNotificationTimeKey] ?: DefaultSettings.defaultLatestNotificationTime.toPreferencesStorageFormat())
        }
    }

    suspend fun setLatestNotificationTime(time: TimeOfDay) {
        context.settingsDataStore.edit { mutablePreferences ->
            mutablePreferences[latestNotificationTimeKey] = time.toPreferencesStorageFormat()
        }
    }
}