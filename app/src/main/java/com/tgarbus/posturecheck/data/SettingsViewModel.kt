package com.tgarbus.posturecheck.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel(): ViewModel() {
    fun getNotificationsPerDay(context: Context): Flow<Int> {
        return SettingsRepository(context).getNotificationsPerDayAsFlow()
    }

    fun setNotificationsPerDay(context: Context, notificationsPerDay: Int) {
        viewModelScope.launch {
            SettingsRepository(context).setNotificationsPerDay(notificationsPerDay)
        }
    }

    fun getEarliestNotificationTime(context: Context): Flow<TimeOfDay> {
        return SettingsRepository(context).getEarliestNotificationTimeAsFlow()
    }

    fun setEarliestNotificationTime(context: Context, time: TimeOfDay) {
        viewModelScope.launch {
            SettingsRepository(context).setEarliestNotificationTime(time)
        }
    }

    fun getLatestNotificationTime(context: Context): Flow<TimeOfDay> {
        return SettingsRepository(context).getLatestNotificationTimeAsFlow()
    }

    fun setLatestNotificationTime(context: Context, time: TimeOfDay) {
        viewModelScope.launch {
            SettingsRepository(context).setLatestNotificationTime(time)
        }
    }
}