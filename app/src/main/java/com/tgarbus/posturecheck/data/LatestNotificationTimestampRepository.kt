package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// EEEEEEEEEEEKKKKKKKKKK
//
// The entire point of this repository is to expose a Flow which changes value every time a new
// notification is sent, to force recomposition and possibly show in-app notification too.
//
// EEEEEEEEEEEKKKKKKKKKK
val Context.latestNotificationTimestampDataStore: DataStore<Preferences> by preferencesDataStore("LatestNotificationTimestamp")
class LatestNotificationTimestampRepository(private val context: Context) {
    private val latestNotificationTimestampKey: Preferences.Key<Int> = intPreferencesKey("latest_notification_timestamp")

    suspend fun setLastNotificationTimestamp(timestamp: Int) {
        context.latestNotificationTimestampDataStore.edit { mutablePreferences ->
            mutablePreferences[latestNotificationTimestampKey] = timestamp
        }
    }

    fun getLastNotificationTimestampAsFlow(): Flow<Int?> {
        return context.latestNotificationTimestampDataStore.data.map { preferences ->
            preferences[latestNotificationTimestampKey]
        }
    }
}