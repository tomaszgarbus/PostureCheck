package com.tgarbus.posturecheck

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tgarbus.posturecheck.data.Day
import com.tgarbus.posturecheck.data.LatestNotificationTimestampRepository
import com.tgarbus.posturecheck.data.PastChecksRepository
import com.tgarbus.posturecheck.data.PastPostureCheck
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.TimeOfDay
import java.lang.Integer.max
import java.util.Calendar
import kotlin.random.Random.Default.nextInt

const val kTestNotificationId = -1
const val kTestNotificationTag = "test"
const val kNotificationText = "Hey, how's your posture?"
const val kChecksNotificationChannel = "Posture Checks"

fun createNotificationChannel(context: Context) {
    val name = "my notification channel"
    val descriptionText = "my freaking notification channel"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(kChecksNotificationChannel, name, importance).apply {
        description = descriptionText
    }
    // Register the channel with the system.
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}

// Builds a pending intent to be executed when user takes any action. The notification will then
// be dismissed by NotificationResponseService.
fun buildPendingIntentForDismissal(context: Context): PendingIntent {
    val baseIntent = Intent(context, NotificationResponseService::class.java)
    return baseIntent.let { intent ->
        val bundle = Bundle()
        bundle.putBoolean("is_test_notification", true)
        intent.putExtras(bundle)
        // TODO: Comment why FLAG_UPDATE_CURRENT and FLAG_MUTABLE are needed.
        val requestCode = -1
        PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
}

suspend fun sendTestNotification(context: Context) {
    val builder = NotificationCompat.Builder(context, kChecksNotificationChannel)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(kNotificationText)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .addAction(R.drawable.ic_launcher_foreground, "Good",
            buildPendingIntentForDismissal(context))
        .addAction(R.drawable.ic_launcher_foreground, "Bad",
            buildPendingIntentForDismissal(context))
        .addAction(R.drawable.ic_launcher_foreground, "N/A",
            buildPendingIntentForDismissal(context))
    builder.setOngoing(true)
    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
            //                                        grantResults: IntArray)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.i("tomek", "nie mam pozwolen")
            return@with
        }
        Log.i("tomek", "zawiadamiam")
        // notificationId is a unique int for each notification that you must define.
        notify(kTestNotificationTag, kTestNotificationId, builder.build())
        context.sendBroadcast(Intent(context, RecomputeNextNotificationsBroadcastReceiver::class.java))
        LatestNotificationTimestampRepository(context).setLastNotificationTimestamp((System.currentTimeMillis() / 1000).toInt())
    }
}

// Currently displayed posture check.
fun getCurrentPostureCheckId(context: Context, latestNotificationTimestamp: Int?): String? {
    with(NotificationManagerCompat.from(context)) {
        for (notification in activeNotifications) {
            val checkId = notification.tag
            return checkId
        }
    }
    return null
}

suspend fun storeReplyAndCancelNotification(
    context: Context,
    pastPostureCheck: PastPostureCheck,
    showToast: Boolean = false
) {
    val plannedChecksRepo = PlannedChecksRepository(context)
    val pastChecksRepo = PastChecksRepository(context)
    val plannedCheck = pastPostureCheck.withoutReply()

    // First cancel the notification.
    with(NotificationManagerCompat.from(context)) {
        cancel(pastPostureCheck.planned.id, pastPostureCheck.notificationId())
    }

    // Now update the repository.
    pastChecksRepo.addPastCheck(pastPostureCheck)
    plannedChecksRepo.deletePlannedCheck(plannedCheck)
    LatestNotificationTimestampRepository(context).setLastNotificationTimestamp(
        (System.currentTimeMillis() / 1000).toInt()
    )
    if (showToast) {
        Toast.makeText(context, "Response saved!", Toast.LENGTH_SHORT).show()
    }
}

suspend fun dismissTestNotification(
    context: Context
) {
    with(NotificationManagerCompat.from(context)) {
        cancel(kTestNotificationTag, kTestNotificationId)
    }
    LatestNotificationTimestampRepository(context).setLastNotificationTimestamp(
        (System.currentTimeMillis() / 1000).toInt()
    )
}

fun scheduleAlarm(context: Context, plannedPostureCheck: PlannedPostureCheck) {
    Log.d("tomek", "Scheduling alarm for $plannedPostureCheck")
    val a: AlarmManager = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, NotificationAlarmBroadcastReceiver::class.java).let { intent ->
        intent.putExtras(plannedPostureCheck.toBundle())
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
    val canScheduleExactAlarms = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || a.canScheduleExactAlarms()
    Log.d("tomek", "Can schedule exact alarms: $canScheduleExactAlarms")
    if (canScheduleExactAlarms) {
        a.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, plannedPostureCheck.millis, alarmIntent)
    } else {
        a.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            plannedPostureCheck.millis,
            alarmIntent)
    }
}

fun recomputeNotificationsForDay(
    notificationsPerDay: Int,
    day: Day,
    minTime: TimeOfDay,
    maxTime: TimeOfDay): HashSet<PlannedPostureCheck> {
    val checks = HashSet<PlannedPostureCheck>()
    val range = minTime.rangeTo(maxTime)
    val cal = Calendar.getInstance()
    cal.timeInMillis = day.toMillis()
    for (i in 1..notificationsPerDay) {
        // TODO: prevent duplicates or too close checks.
        val timeOfDay = range[nextInt(0, range.size)]
        cal.set(Calendar.HOUR_OF_DAY, timeOfDay.hour)
        cal.set(Calendar.MINUTE, timeOfDay.minute)
        val check = PlannedPostureCheck(millis = cal.timeInMillis)
        checks.add(check)
    }
    return checks
}

suspend fun addCheck(context: Context, plannedPostureCheck: PlannedPostureCheck) {
    PlannedChecksRepository(context).addPlannedCheck(plannedPostureCheck)
}

suspend fun addAndScheduleCheck(context: Context, plannedPostureCheck: PlannedPostureCheck) {
    PlannedChecksRepository(context).addPlannedCheck(plannedPostureCheck)
    scheduleAlarm(context, plannedPostureCheck)
}

suspend fun addAndScheduleCheckAtTime(context: Context, millis: Long) {
    val newCheck = PlannedPostureCheck(millis)
    addAndScheduleCheck(context, newCheck)
}

// Schedule checks at the first run of the app.
suspend fun scheduleChecksFirstDay(
    context: Context, notificationsPerDay: Int, minTime: TimeOfDay, maxTime: TimeOfDay, triggerRecompute: () -> Unit) {
    addAndScheduleCheckAtTime(context, System.currentTimeMillis())
    var notificationsToSchedule = max(0, notificationsPerDay - 1)
    var minTime = minTime
    val now = TimeOfDay.now()
    if (now > minTime) {
        minTime = now
    }
    if (minTime > maxTime) {
        notificationsToSchedule = 0
    }
    val plannedChecks = recomputeNotificationsForDay(
        notificationsToSchedule, Day.today(), minTime, maxTime
    )
    for (check in plannedChecks) {
        addCheck(context, check)
    }
    // triggerRecompute()
}

// Admin-only!
suspend fun scheduleRealCheckNSecondsFromNow(context: Context, nSeconds: Int) {
    addAndScheduleCheckAtTime(context, System.currentTimeMillis() + nSeconds * 1000)
}