package com.tgarbus.posturecheck.data

import android.util.Log
import com.tgarbus.posturecheck.ui.reusables.LineChartEntry

data class AnswersDistribution(
    var goodPostureCount: Int,
    var badPostureCount: Int,
    var noAnswerCount: Int,
)

private fun AnswersDistribution.sumCounts(): Int {
    return goodPostureCount + badPostureCount + noAnswerCount
}

fun AnswersDistribution.percentGood(): Int {
    return 100 * goodPostureCount / sumCounts()
}

fun AnswersDistribution.percentBad(): Int {
    return 100 * badPostureCount / sumCounts()
}

fun AnswersDistribution.percentNoAnswer(): Int {
    return 100 * noAnswerCount / sumCounts()
}

fun AnswersDistribution.increment(reply: PostureCheckReply) {
    when (reply) {
        PostureCheckReply.GOOD -> goodPostureCount++
        PostureCheckReply.BAD -> badPostureCount++
        PostureCheckReply.NO_ANSWER -> noAnswerCount++
        PostureCheckReply.NOT_APPLICABLE -> noAnswerCount++
    }
}

data class StatsForPeriod(
    val answersDistribution: AnswersDistribution,
    val chartEntries: List<LineChartEntry>
)

enum class PeriodType {
    WEEK,
    MONTH,
    ALL_TIME,
}

fun buildLineChartEntry(day: Day, answersDistribution: AnswersDistribution): LineChartEntry {
    return LineChartEntry(
        value = answersDistribution.percentGood() / 100f,
        label = day.getDayOfWeek()
    )
}

fun buildLastWeekChartEntries(
    pastPostureChecks: Collection<PastPostureCheck>,
    includeToday: Boolean): ArrayList<LineChartEntry>? {
    val aggregates = HashMap<Day, AnswersDistribution>()
    val days = lastWeek(includeToday)
    for (check in pastPostureChecks) {
        if (!aggregates.contains(check.planned.getDay())) {
            aggregates[check.planned.getDay()] = AnswersDistribution(0, 0, 0)
        }
        aggregates[check.planned.getDay()]!!.increment(check.reply)
    }
    val entries = ArrayList<LineChartEntry>()
    for (day in days) {
        if (!aggregates.contains(day)) {
            Log.d("tomek", "Failed to build aggregate for ${day}")
            return null
        }
        entries.add(buildLineChartEntry(day, aggregates[day]!!))
    }
    return entries
}

private fun lastDays(includeToday: Boolean, nDays: Int): List<Day> {
    var day = Day.today()
    val result = ArrayList<Day>()
    if (!includeToday) {
        day -= 1
    }
    for (i in 1..nDays) {
        result.add(day)
        day -= 1
    }
    return result.reversed()
}

fun lastWeek(includeToday: Boolean): List<Day> {
    return lastDays(includeToday, 7)
}

fun buildAnswersDistribution(pastPostureChecks: Collection<PastPostureCheck>): AnswersDistribution? {
    val counts = HashMap<PostureCheckReply, Int>()
    for (reply in PostureCheckReply.entries) {
        counts[reply] = 0
    }
    for (check in pastPostureChecks) {
        counts[check.reply] = counts[check.reply]!! + 1
    }
    if (counts[PostureCheckReply.GOOD]!! == 0) {
        return null
    }
    return AnswersDistribution(
        goodPostureCount = counts[PostureCheckReply.GOOD]!!,
        badPostureCount = counts[PostureCheckReply.BAD]!!,
        noAnswerCount = counts[PostureCheckReply.NO_ANSWER]!! + counts[PostureCheckReply.NOT_APPLICABLE]!!
    )
}