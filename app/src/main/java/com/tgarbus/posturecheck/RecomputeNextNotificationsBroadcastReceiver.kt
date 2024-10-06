package com.tgarbus.posturecheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.TimeOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

class RecomputeNextNotificationsBroadcastReceiver : BroadcastReceiver() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    private fun validateNotificationsForDay(
        notifications: Set<PlannedPostureCheck>,
        notificationsPerDay: Int = 3,
        minTime: TimeOfDay = TimeOfDay(8, 0),
        maxTime: TimeOfDay = TimeOfDay(21, 0)
    ): Boolean {
        // Validate number of notifications.
        if (notifications.size != notificationsPerDay) {
            return false
        }

        // Validate min time.
        val earliestTime = notifications.minBy { it.getTimeOfDay() }.getTimeOfDay()
        if (earliestTime < minTime) {
            return false
        }

        // Validate max time.
        val latestTime = notifications.maxBy { it.getTimeOfDay() }.getTimeOfDay()
        if (latestTime > maxTime) {
            return false
        }

        return true
    }

    // TODO: handle different locale
    // TOOD: recompute if user extended min and max time frame
    private fun recomputeNextNotifications(
        nextNotifications: Set<PlannedPostureCheck>,
        daysAhead: Int = 5,
        notificationsPerDay: Int = 3,
        minTime: TimeOfDay = TimeOfDay(8, 0),
        maxTime: TimeOfDay = TimeOfDay(21, 0)
        // TODO: max time
    ): Set<PlannedPostureCheck> {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Set up formatter.
        val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
        sdf.timeZone = calendar.getTimeZone()

        // First group notifications per day for the upcoming days.
        val groupedByDay = HashMap<String, HashSet<PlannedPostureCheck>>()
        for (plannedCheck in nextNotifications) {
            groupedByDay.getOrDefault(plannedCheck.formatDate(sdf), HashSet()).add(plannedCheck)
        }

        // Recompute days which have wrong number of notifications or don't comply with
        // preferred times.
        for (val i = 0.)

        // Finally flatten the grouped notifications into a hashset.


        return HashSet()
    }

    private fun scheduleAlarm(context: Context, plannedPostureCheck: PlannedPostureCheck) {
        val a: AlarmManager = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        Log.i("tomek", "building intent")
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

    override fun onReceive(context: Context, intent: Intent) {
        // First recompute checks and update repository.
        val plannedChecksRepo = PlannedChecksRepository(context)
        var oldPlannedChecks: Set<PlannedPostureCheck>? = null
        runBlocking {
            oldPlannedChecks = plannedChecksRepo.getPlannedChecks()
        }
        val newPlannedChecks = recomputeNextNotifications(
            oldPlannedChecks!!
        )
        for (check in newPlannedChecks) {
            if (!oldPlannedChecks!!.contains(check)) {
                runBlocking {
                    plannedChecksRepo.addPlannedCheck(check)
                }
            }
        }
        for (check in oldPlannedChecks!!) {
            if (!newPlannedChecks.contains(check)) {
                runBlocking {
                    plannedChecksRepo.deletePlannedCheck(check)
                }
            }
        }

        // Second pick the earliest upcoming check.
        val plannedPostureCheck = PlannedPostureCheck(
            id = UUID.randomUUID().toString(),
            millis = System.currentTimeMillis() + 10000
        )

        // Last schedule the earliest check.
        scheduleAlarm(context, plannedPostureCheck)
    }
}