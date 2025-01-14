package com.tgarbus.posturecheck

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tgarbus.posturecheck.data.Day
import com.tgarbus.posturecheck.data.DefaultSettings
import com.tgarbus.posturecheck.data.PlannedChecksRepository
import com.tgarbus.posturecheck.data.PlannedPostureCheck
import com.tgarbus.posturecheck.data.SettingsRepository
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.data.validateNotificationsForDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar

class RecomputeNextNotificationsBroadcastReceiver : BroadcastReceiver() {
    // TODO: handle different locale
    private fun recomputeNextNotifications(
        nextNotifications: Set<PlannedPostureCheck>,
        daysAhead: Int = 5,
        notificationsPerDay: Int = DefaultSettings.defaulNotificationsPerDay,
        minTime: TimeOfDay = DefaultSettings.defaultEarliestNotificationTime,
        maxTime: TimeOfDay = DefaultSettings.defaultLatestNotificationTime
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
        // TODO: refactor using the Day data model.
        for (i in 1..daysAhead) {
            calendar.add(Calendar.DATE, 1)
            val dayStr = sdf.format(calendar.time)
            if (!validateNotificationsForDay(groupedByDay.getOrDefault(dayStr, HashSet()),
                notificationsPerDay, minTime, maxTime)) {
                groupedByDay[dayStr] =
                    recomputeNotificationsForDay(notificationsPerDay, Day.fromMillis(calendar.timeInMillis), minTime, maxTime)
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

    private fun getEarliestFutureCheck(plannedChecks: Set<PlannedPostureCheck>): PlannedPostureCheck {
        val timeNow = System.currentTimeMillis()
        return plannedChecks.filter { it.millis > timeNow }.minBy { it.millis }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val plannedChecksRepo = PlannedChecksRepository(context)
        val settingsRepo = SettingsRepository(context)
        var oldPlannedChecks: Set<PlannedPostureCheck>? = null
        var notificationsPerDay = DefaultSettings.defaulNotificationsPerDay
        var earliestNotificationTime = DefaultSettings.defaultEarliestNotificationTime
        var latestNotificationTime = DefaultSettings.defaultLatestNotificationTime
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