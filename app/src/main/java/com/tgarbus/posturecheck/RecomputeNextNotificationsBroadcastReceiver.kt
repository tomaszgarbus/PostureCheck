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
import com.tgarbus.posturecheck.data.SettingsRepository
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.data.kDefaultEarliestNotificationTime
import com.tgarbus.posturecheck.data.kDefaultLatestNotificationTime
import com.tgarbus.posturecheck.data.kDefaultNotificationsPerDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import kotlin.random.Random.Default.nextInt

class RecomputeNextNotificationsBroadcastReceiver : BroadcastReceiver() {
    private fun validateNotificationsForDay(
        notifications: Set<PlannedPostureCheck>,
        notificationsPerDay: Int,
        minTime: TimeOfDay,
        maxTime: TimeOfDay
    ): Boolean {
        Log.i("tomek", "Validation notifications per day: ${notifications}, ${notificationsPerDay}, ${minTime}, ${maxTime}")

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
        return latestTime <= maxTime
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
        notificationsPerDay: Int = kDefaultNotificationsPerDay,
        minTime: TimeOfDay = TimeOfDay.fromPreferencesStorageFormat(kDefaultEarliestNotificationTime),
        maxTime: TimeOfDay = TimeOfDay.fromPreferencesStorageFormat(kDefaultLatestNotificationTime)
        // TODO: max time
    ): Set<PlannedPostureCheck> {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Set up formatter.
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        sdf.timeZone = calendar.getTimeZone()

        Log.i("tomek", "Recomputing notifications: $nextNotifications")
        // First group notifications per day for the upcoming days.
        val groupedByDay = HashMap<String, HashSet<PlannedPostureCheck>>()
        for (plannedCheck in nextNotifications) {
            val dateStr = plannedCheck.formatDate(sdf)
            if (!groupedByDay.contains(dateStr)) {
                groupedByDay[dateStr] = HashSet()
            }
            groupedByDay[dateStr]!!.add(plannedCheck)
        }
        Log.i("tomek", groupedByDay.toString())

        // Recompute days which have wrong number of notifications or don't comply with
        // preferred times.
        for (i in 1..daysAhead) {
            calendar.add(Calendar.DATE, 1)
            val dayStr = sdf.format(calendar.time)
            if (!validateNotificationsForDay(groupedByDay.getOrDefault(dayStr, HashSet()),
                notificationsPerDay, minTime, maxTime)) {
                Log.i("tomek", "Recomputing notifications for day ${dayStr}")
                groupedByDay[dayStr] =
                    recomputeNotificationsForDay(notificationsPerDay, calendar, minTime, maxTime)
            }
        }

        // Finally flatten the grouped notifications into a hashset.
        val recomputedNotifications = HashSet<PlannedPostureCheck>()
        for (day in groupedByDay.keys) {
            recomputedNotifications.addAll(groupedByDay.getOrDefault(day, HashSet()))
        }

        val debugMissingNotifications = false
        if (debugMissingNotifications) {
            val soon: Calendar = Calendar.getInstance()
            soon.timeInMillis = System.currentTimeMillis() + 100000
            val checkInTenSeconds = PlannedPostureCheck(millis = soon.timeInMillis)
            recomputedNotifications.add(checkInTenSeconds)
        }

        return recomputedNotifications
    }

    private fun scheduleAlarm(context: Context, plannedPostureCheck: PlannedPostureCheck) {
        val a: AlarmManager = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        Log.i("tomek", "building intent")
        Log.i("tomek",
            "RecomputeNextNotificationsService: plannedPostureCheck: $plannedPostureCheck"
        )
        val alarmIntent = Intent(context, NotificationAlarmBroadcastReceiver::class.java).let { intent ->
            intent.putExtras(plannedPostureCheck.toBundle())
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        a.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            plannedPostureCheck.millis,
            alarmIntent)
    }

    private fun getEarliestFutureCheck(plannedChecks: Set<PlannedPostureCheck>): PlannedPostureCheck {
        var earliest = plannedChecks.first()
        val timeNow = System.currentTimeMillis()
        for (check in plannedChecks) {
            if (check.millis > timeNow &&
                check.millis < earliest.millis) {
                earliest = check
            }
        }
        return earliest
    }

    override fun onReceive(context: Context, intent: Intent) {
        val plannedChecksRepo = PlannedChecksRepository(context)
        val settingsRepo = SettingsRepository(context)
        var oldPlannedChecks: Set<PlannedPostureCheck>? = null
        var notificationsPerDay = kDefaultNotificationsPerDay
        var earliestNotificationTime = TimeOfDay.fromPreferencesStorageFormat(
            kDefaultEarliestNotificationTime)
        var latestNotificationTime = TimeOfDay.fromPreferencesStorageFormat(
            kDefaultLatestNotificationTime)
        runBlocking {
            oldPlannedChecks = plannedChecksRepo.getPlannedChecks()
            notificationsPerDay = settingsRepo.getNotificationsPerDayAsFlow().first()
            earliestNotificationTime = settingsRepo.getEarliestNotificationTimeAsFlow().first()
            latestNotificationTime = settingsRepo.getLatestNotificationTimeAsFlow().first()
        }
        val newPlannedChecks = recomputeNextNotifications(
            oldPlannedChecks!!,
            notificationsPerDay = notificationsPerDay,
            minTime = earliestNotificationTime,
            maxTime = latestNotificationTime
        )
        // Add new checks to the repo.
        for (check in newPlannedChecks) {
            if (!oldPlannedChecks!!.contains(check)) {
                runBlocking {
                    plannedChecksRepo.addPlannedCheck(check)
                }
            }
        }
        // Remove old checks from the repo.
        for (check in oldPlannedChecks!!) {
            // We don't touch checks already planned for today.
            if (!newPlannedChecks.contains(check) && !check.isToday()) {
                runBlocking {
                    plannedChecksRepo.deletePlannedCheck(check)
                }
            }
        }
        // TODO: Clean up checks in the past from the repo.

        // Second pick the earliest upcoming check.
        val plannedPostureCheck = getEarliestFutureCheck(newPlannedChecks)

        Log.i("tomek", "Scheduling check: " + plannedPostureCheck.toString())

        // Last schedule the earliest check.
        scheduleAlarm(context, plannedPostureCheck)
    }
}