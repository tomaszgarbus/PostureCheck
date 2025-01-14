package com.tgarbus.posturecheck.data

import android.util.Log
import com.tgarbus.posturecheck.recomputeNotificationsForDay
import java.util.Calendar
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

fun validateNotificationsForDay(
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
    return latestTime <= maxTime
}

fun randomReply(): PostureCheckReply {
    if (nextBoolean()) {
        return PostureCheckReply.BAD
    }
    if (nextBoolean()) {
        return PostureCheckReply.GOOD
    }
    if (nextBoolean()) {
        return PostureCheckReply.NOT_APPLICABLE
    }
    return PostureCheckReply.NO_ANSWER
}

fun generateFakePastChecks(
    numDaysBack: Int = 30,
    numChecksPerDay: Int = 3,
): ArrayList<PastPostureCheck> {
    val days = Day.range(Day.today() - numDaysBack, Day.today() - 1)
    val result = ArrayList<PastPostureCheck>()
    for (day in days) {
        val planned = recomputeNotificationsForDay(
            numChecksPerDay, day, TimeOfDay(8, 0), TimeOfDay(21, 0)
        )
        result.addAll(planned.map { plannedCheck -> PastPostureCheck(plannedCheck, randomReply()) })
    }
    return result
}