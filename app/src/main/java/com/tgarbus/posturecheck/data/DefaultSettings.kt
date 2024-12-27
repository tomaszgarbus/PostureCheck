package com.tgarbus.posturecheck.data

class DefaultSettings {
    companion object {
        val defaultEarliestNotificationTime = TimeOfDay(8, 0)
        val defaultLatestNotificationTime = TimeOfDay(21, 0)
        val defaulNotificationsPerDay = 3
    }
}