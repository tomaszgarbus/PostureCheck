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
import kotlin.random.Random.Default.nextInt

class RecomputeNextNotificationsBroadcastReceiver : BroadcastReceiver() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    private fun validateNotificationsForDay(
        notifications: Set<PlannedPostureCheck>,
        notificationsPerDay: Int,
        minTime: TimeOfDay,
        maxTime: TimeOfDay
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

    private fun recomputeNotificationsForDay(
        notificationsPerDay: Int,
        getDayFrom: Calendar,
        minTime: TimeOfDay,
        maxTime: TimeOfDay): HashSet<PlannedPostureCheck> {
        val checks = HashSet<PlannedPostureCheck>()
        val range = minTime.rangeTo(maxTime)
        val cal = Calendar.getInstance()
        cal.timeInMillis = getDayFrom.timeInMillis
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
        for (i in 1..daysAhead) {
            calendar.add(Calendar.DATE, 1)
            val dayStr = sdf.format(calendar.time)
            if (!validateNotificationsForDay(groupedByDay.getOrDefault(dayStr, HashSet()),
                notificationsPerDay, minTime, maxTime)) {
                groupedByDay[dayStr] =
                    recomputeNotificationsForDay(notificationsPerDay, calendar, minTime, maxTime)
            }
        }

        // Finally flatten the grouped notifications into a hashset.
        val recomputedNotifications = HashSet<PlannedPostureCheck>()
        for (day in groupedByDay) {
            recomputedNotifications.addAll(groupedByDay.getOrDefault(day, HashSet()))
        }

        return recomputedNotifications
    }

    private fun scheduleAlarm(context: Context, plannedPostureCheck: PlannedPostureCheck) {
        val a: AlarmManager = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        Log.i("tomek", "building intent")
        Log.i("tomek",
            "RecomputeNextNotificationsService: plannedPostureCheck: $plannedPostureCheck"
        )
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

    private fun getEarliestCheck(plannedChecks: Set<PlannedPostureCheck>): PlannedPostureCheck {
        var earliest = plannedChecks.first()
        for (check in plannedChecks) {
            if (check.millis < earliest.millis) {
                earliest = check
            }
        }
        return earliest
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
        val plannedPostureCheck = getEarliestCheck(newPlannedChecks)

        // Last schedule the earliest check.
        scheduleAlarm(context, plannedPostureCheck)
    }
}