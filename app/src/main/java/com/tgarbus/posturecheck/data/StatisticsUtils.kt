package com.tgarbus.posturecheck.data

import com.tgarbus.posturecheck.ui.reusables.LineChartEntry
import com.tgarbus.posturecheck.ui.reusables.WeekGridChartColumn
import com.tgarbus.posturecheck.ui.reusables.WeekGridChartEntry

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

fun buildLineChartEntry(label: String, showLabelOnAxis: Boolean, answersDistribution: AnswersDistribution): LineChartEntry {
    return LineChartEntry(
        value = answersDistribution.percentGood() / 100f,
        label = label,
        showLabelOnAxis = showLabelOnAxis,
    )
}

fun buildLineChartEntriesForDays(
    days: List<Day>,
    pastPostureChecks: Collection<PastPostureCheck>,
    dayToLabel: (Day) -> String,
    dayToShowLabel: (Day) -> Boolean,
): ArrayList<LineChartEntry>? {
    val aggregates = HashMap<Day, AnswersDistribution>()
    for (check in pastPostureChecks) {
        if (!aggregates.contains(check.planned.getDay())) {
            aggregates[check.planned.getDay()] = AnswersDistribution(0, 0, 0)
        }
        aggregates[check.planned.getDay()]!!.increment(check.reply)
    }
    val entries = ArrayList<LineChartEntry>()
    for (day in days) {
        if (!aggregates.contains(day)) {
            return null
        }
        entries.add(buildLineChartEntry(dayToLabel(day), dayToShowLabel(day), aggregates[day]!!))
    }
    return entries
}

fun buildLastWeekLineChartEntries(
    pastPostureChecks: Collection<PastPostureCheck>,
    includeToday: Boolean): ArrayList<LineChartEntry>? {
    val days = lastWeek(includeToday)
    return buildLineChartEntriesForDays(
        days, pastPostureChecks, { it.getDayOfWeek() }, { true })
}

fun buildLastMonthLineChartEntries(
    pastPostureChecks: Collection<PastPostureCheck>,
    includeToday: Boolean
): ArrayList<LineChartEntry>? {
    val days = lastMonth(includeToday)
    return buildLineChartEntriesForDays(days, pastPostureChecks, { it.toShortString() }, { it.getDayOfWeek() == "Mon" })
}

fun buildLineChartEntriesForPeriod(
    period: PeriodType,
    checks: Collection<PastPostureCheck>,
    includeToday: Boolean
): ArrayList<LineChartEntry>? {
    return when (period) {
        PeriodType.WEEK -> buildLastWeekLineChartEntries(checks, includeToday)
        PeriodType.MONTH -> buildLastMonthLineChartEntries(checks, includeToday)
        PeriodType.ALL_TIME -> null
    }
}

fun groupPastChecksByDay(checks: Collection<PastPostureCheck>): HashMap<Day, ArrayList<PastPostureCheck>> {
    val result = HashMap<Day, ArrayList<PastPostureCheck>>()
    for (check in checks) {
        val day = check.planned.getDay()
        if (!result.contains(day)) {
            result[day] = arrayListOf()
        }
        result[day]!!.add(check)
    }
    for (day in result.keys) {
        result[day]!!.sortBy { it.planned.getDay() }
    }
    return result
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

fun lastMonth(includeToday: Boolean): List<Day> {
    return lastDays(includeToday, 28)
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

fun buildWeekGridChartColumns(
    checks: Collection<PastPostureCheck>, includeToday: Boolean): ArrayList<WeekGridChartColumn>? {
    val days = lastWeek(includeToday)
    val groupedByDay = groupPastChecksByDay(checks)
    for (day in days) {
        if (!groupedByDay.contains(day)) {
            return null
        }
    }
    val result: ArrayList<WeekGridChartColumn> = arrayListOf()
    for (day in days) {
        val column = WeekGridChartColumn(
            label = day.getDayOfWeek(),
            entries = ArrayList(groupedByDay[day]!!.map {
                WeekGridChartEntry(time = it.planned.getTimeOfDay(), reply = it.reply)
            })
        )
        result.add(column)
    }
    return result
}