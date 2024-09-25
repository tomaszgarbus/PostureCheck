package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.TimeOfDay
import java.util.UUID

class RecomputeNextNotificationsService : Service() {

  private fun recomputeNextNotifications(
    daysAhead: Int = 5,
    notificationsPerDay: Int = 3,
    minTime: TimeOfDay = TimeOfDay(8, 0),
    maxTime: TimeOfDay = TimeOfDay(21, 0)
    // TODO: max time
  ): HashSet<PlannedPostureCheck> {
    return HashSet()
  }

  override fun onCreate() {
    super.onCreate()
    val a: AlarmManager = baseContext!!.getSystemService(ALARM_SERVICE) as AlarmManager
    Log.i("tomek", "building intent")
    val plannedPostureCheck = PlannedPostureCheck(
      id = UUID.randomUUID().toString(),
      millis = System.currentTimeMillis() + 10000
    )
    Log.i("tomek", "RecomputeNextNotificationsService: plannedPostureCheck: " + plannedPostureCheck.toString())
    // TODO: Create the planned check in repo. This requires creating a view model.
    val alarmIntent = Intent(baseContext, NotificationAlarmBroadcastReceiver::class.java).let { intent ->
      intent.putExtras(plannedPostureCheck.toBundle())
      PendingIntent.getBroadcast(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    a.set(
      AlarmManager.RTC_WAKEUP,
      plannedPostureCheck.millis,
      alarmIntent)

    stopSelf()
  }

  override fun onBind(intent: Intent): IBinder? {
    return null
  }
}