package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.TimeOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch.
import java.util.UUID

class RecomputeNextNotificationsBroadcastReceiver : BroadcastReceiver() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    private fun recomputeNextNotifications(
        daysAhead: Int = 5,
        notificationsPerDay: Int = 3,
        minTime: TimeOfDay = TimeOfDay(8, 0),
        maxTime: TimeOfDay = TimeOfDay(21, 0)
        // TODO: max time
    ): HashSet<PlannedPostureCheck> {
        val today =
        return HashSet()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        GlobalScope.launch {

        }

        // Scheduling alarm.
        val a: AlarmManager = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        Log.i("tomek", "building intent")
        val plannedPostureCheck = PlannedPostureCheck(
            id = UUID.randomUUID().toString(),
            millis = System.currentTimeMillis() + 10000
        )
        Log.i("tomek", "RecomputeNextNotificationsService: plannedPostureCheck: " + plannedPostureCheck.toString())
        // TODO: Create the planned check in repo. This requires creating a view model.
        val alarmIntent = Intent(context, NotificationAlarmBroadcastReceiver::class.java).let { intent ->
            intent.putExtras(plannedPostureCheck.toBundle())
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        a.set(
            AlarmManager.RTC_WAKEUP,
            plannedPostureCheck.millis,
            alarmIntent)
    }
}