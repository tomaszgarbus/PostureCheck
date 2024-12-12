package com.tgarbus.posturecheck.data

import android.util.Log
import java.util.Calendar
import kotlin.random.Random.Default.nextBoolean
import kotlin.random.Random.Default.nextInt

fun validateNotificationsForDay(
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