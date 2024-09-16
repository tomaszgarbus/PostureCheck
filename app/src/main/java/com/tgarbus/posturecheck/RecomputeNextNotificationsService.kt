package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import java.util.UUID

class RecomputeNextNotificationsService : Service() {

  override fun onCreate() {
    super.onCreate()
    val a: AlarmManager = baseContext!!.getSystemService(ALARM_SERVICE) as AlarmManager
    Log.i("tomek", "building intent")
    val plannedPostureCheck = PlannedPostureCheck(
      id = UUID.randomUUID().toString(),
      millis = System.currentTimeMillis() + 10000
    )
    // TODO: Create the planned check in repo. This requires creating a view model.
    val alarmIntent = Intent(baseContext, NotificationAlarmBroadcastReceiver::class.java).let { intent ->
      // TODO: Put the planned check here.
      intent.putExtras(plannedPostureCheck.toBundle())
      PendingIntent.getBroadcast(baseContext, 0, intent, 0)
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