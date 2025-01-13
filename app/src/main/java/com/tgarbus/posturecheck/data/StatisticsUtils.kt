package com.tgarbus.posturecheck.data

import android.util.Log
import com.tgarbus.posturecheck.ui.reusables.LineChartEntry
import com.tgarbus.posturecheck.ui.reusables.GridChartColumnInput
import com.tgarbus.posturecheck.ui.reusables.GridChartEntryInput
import java.util.Collections.min

// Number of GOOD/BAD answers required to show the stats.
const val kShowStatsThreshold = 2

data class AnswersDistribution(
    var goodPostureCount: Int,
    var badPostureCount: Int,
    var noAnswerCount: Int,
) {
    companion object {
        fun fromChecks(checks: Collection<PastPostureCheck>): AnswersDistribution {
            var goodPostureCount = 0
            var badPostureCount = 0
            var noAnswerCount = 0
            for (check in checks) {
                when (check.reply) {
                    PostureCheckReply.GOOD -> goodPostureCount += 1
                    PostureCheckReply.BAD -> badPostureCount += 1
                    else -> noAnswerCount += 1
                }
            }
            return AnswersDistribution(goodPostureCount, badPostureCount, noAnswerCount)
        }
    }
}

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
    bestEffort: Boolean = true
): ArrayList<LineChartEntry>? {
    val aggregates = HashMap<Day, AnswersDistribution>()
    for (check in pastPostureChecks) {
        if (!aggregates.contains(check.planned.getDay())) {
            aggregates[check.planned.getDay()] = AnswersDistribution(0, 0, 0)
        }
        aggregates[check.planned.getDay()]!!.increment(check.reply)
    }
    val entries = ArrayList<LineChartEntry>()
    if (bestEffort) {
        var previousAggregate: AnswersDistribution? = null
        for (day in days) {
            val aggregate = aggregates[day] ?: previousAggregate
            previousAggregate = aggregate
            Log.d("tomek", "debug line chart building: $day $aggregate")
            aggregate?.let {
                entries.add(
                    buildLineChartEntry(
                        dayToLabel(day),
                        dayToShowLabel(day),
                        it
                    )
                )
            }
        }
    } else {
        for (day in days) {
            if (!aggregates.contains(day)) {
                return null
            }
            entries.add(
                buildLineChartEntry(
                    dayToLabel(day),
                    dayToShowLabel(day),
                    aggregates[day]!!
                )
            )
        }
    }
    if (entries.size > 1) {
        return entries
    }
    return null
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

fun collectChecksFromInterval(
    groupedByDay: HashMap<Day, ArrayList<PastPostureCheck>>, dayFrom: Day,
    dayTo: Day): ArrayList<PastPostureCheck> {
    var day = dayFrom
    val result = ArrayList<PastPostureCheck>()
    while (day <= dayTo) {
        if (groupedByDay.contains(day)) {
            result.addAll(groupedByDay[day]!!)
        }
        day += 1
    }
    return result
}

fun buildLineChartEntriesForAllTime(
    pastPostureChecks: Collection<PastPostureCheck>,
    includeToday: Boolean,
    numEntries: Int = 30,
    numShownLabels: Int = 8,
    bestEffort: Boolean = true,
): ArrayList<LineChartEntry>? {
    val earliestDay = pastPostureChecks.minOf { it.planned.getDay() }
    var latestDay = Day.today()
    if (!includeToday) {
        latestDay -= 1
    }
    val totalDays = latestDay - earliestDay + 1
    val daysPerEntry = totalDays / numEntries + if (totalDays % numEntries > 0) 1 else 0

    val groupedByDay = groupPastChecksByDay(pastPostureChecks)
    var dayFrom = earliestDay
    val result = ArrayList<LineChartEntry>()
    val showLabelEveryNth = numEntries / numShownLabels + if (numEntries % numShownLabels > 0) 1 else 0
    var prevAggregate: AnswersDistribution? = null
    while (dayFrom <= latestDay) {
        val dayTo = min(listOf(dayFrom + daysPerEntry - 1, latestDay))
        val checks = collectChecksFromInterval(groupedByDay, dayFrom, dayTo)
        var distribution: AnswersDistribution? = AnswersDistribution.fromChecks(checks)
        if (checks.isEmpty()) {
            if (bestEffort) {
                distribution = prevAggregate
            } else {
                return null
            }
        }
        val showLabel = result.size % showLabelEveryNth == 0
        result.add(buildLineChartEntry(dayFrom.toShortString(), showLabel, distribution!!))
        dayFrom += daysPerEntry
        prevAggregate = distribution
    }
    return result
}

fun buildLineChartEntriesForPeriod(
    period: PeriodType,
    checks: Collection<PastPostureCheck>,
    includeToday: Boolean
): ArrayList<LineChartEntry>? {
    return when (period) {
        PeriodType.WEEK -> buildLastWeekLineChartEntries(checks, includeToday)
        PeriodType.MONTH -> buildLastMonthLineChartEntries(checks, includeToday)
        PeriodType.ALL_TIME -> buildLineChartEntriesForAllTime(checks, includeToday)
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
    checks: Collection<PastPostureCheck>, includeToday: Boolean, bestEffort: Boolean = true): ArrayList<GridChartColumnInput>? {
    val days = lastWeek(includeToday)
    val groupedByDay = groupPastChecksByDay(checks)
    if (!bestEffort) {
        for (day in days) {
            if (!groupedByDay.contains(day)) {
                return null
            }
        }
    }
    val result: ArrayList<GridChartColumnInput> = arrayListOf()
    for (day in days) {
        val entriesForDay = groupedByDay[day]
        val column = GridChartColumnInput(
            label = day.getDayOfWeek(),
            entries = ArrayList(entriesForDay?.map {
                GridChartEntryInput(time = it.planned.getTimeOfDay(), reply = it.reply)
            } ?: arrayListOf())
        )
        result.add(column)
    }
    if (result.size < 2) {
        return null
    }
    return result
}

fun buildMonthGridChartColumns(
    checks: Collection<PastPostureCheck>, includeToday: Boolean, bestEffort: Boolean = true): ArrayList<GridChartColumnInput>? {
    val days = lastMonth(includeToday)
    val groupedByDay = groupPastChecksByDay(checks)
    if (!bestEffort) {
        for (day in days) {
            if (!groupedByDay.contains(day)) {
                return null
            }
        }
    }
    val result: ArrayList<GridChartColumnInput> = arrayListOf()
    for ((i, day) in days.withIndex()) {
        val column = GridChartColumnInput(
            label = if (i % 7 == 0) day.toShortString() else null,
            entries = ArrayList(groupedByDay[day]?.map {
                GridChartEntryInput(time = it.planned.getTimeOfDay(), reply = it.reply)
            } ?: arrayListOf())
        )
        result.add(column)
    }
    return result
}

fun buildAllTimeGridChartColumns(
    checks: Collection<PastPostureCheck>, includeToday: Boolean,
    showLabelEveryNth: Int = 7): ArrayList<GridChartColumnInput>? {
    val groupedByDay = groupPastChecksByDay(checks)
    val result: ArrayList<GridChartColumnInput> = arrayListOf()
    val earliestDay = checks.minOf { it.planned.getDay() }
    var latestDay = Day.today()
    if (!includeToday) {
        latestDay -= 1
    }
    var day = earliestDay
    var i = 0
    while (day <= latestDay) {
        val column = GridChartColumnInput(
            label = if (i % showLabelEveryNth == 0) day.toString() else null,
            entries = ArrayList(groupedByDay[day]?.map {
                GridChartEntryInput(time = it.planned.getTimeOfDay(), reply = it.reply)
            } ?: arrayListOf())
        )
        result.add(column)
        day += 1
        i++
    }
    if (result.size < 2) {
        return null
    }
    return result
}

fun buildGridChartColumnsForPeriod(
    checks: Collection<PastPostureCheck>,
    period: PeriodType, includeToday: Boolean
): ArrayList<GridChartColumnInput>? {
    return when (period) {
        PeriodType.WEEK -> buildWeekGridChartColumns(checks, includeToday)
        PeriodType.MONTH -> buildMonthGridChartColumns(checks, includeToday)
        PeriodType.ALL_TIME -> buildAllTimeGridChartColumns(checks, includeToday)
    }
}

fun shouldOnlyShowBePatientBanner(
    answersDistribution: AnswersDistribution?
): Boolean {
    if (answersDistribution == null) {
        return true
    }
    return answersDistribution.goodPostureCount + answersDistribution.badPostureCount < kShowStatsThreshold
}