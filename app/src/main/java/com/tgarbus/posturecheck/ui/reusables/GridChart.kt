package com.tgarbus.posturecheck.ui.reusables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.PostureCheckReply
import com.tgarbus.posturecheck.data.TimeOfDay
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import kotlin.math.max
import kotlin.math.min

data class WeekGridChartEntry(
    val time: TimeOfDay,
    val reply: PostureCheckReply
)

data class WeekGridChartColumn(
    val label: String,
    val entries: ArrayList<WeekGridChartEntry>
) {
    fun entriesBeforeTime(time: TimeOfDay): ArrayList<WeekGridChartEntry> {
        return ArrayList(entries.filter { it.time < time }.sortedBy { it.time })
    }

    fun entriesAfterTime(time: TimeOfDay): ArrayList<WeekGridChartEntry> {
        return ArrayList(entries.filter { it.time > time }.sortedBy { it.time })
    }

    fun entriesBetweenTimes(minTime: TimeOfDay, maxTime: TimeOfDay): ArrayList<WeekGridChartEntry> {
        return ArrayList(entries.filter { it.time in minTime..maxTime }.sortedBy { it.time })
    }

    fun countEntriesBeforeTime(time: TimeOfDay): Int {
        return entries.count { it.time < time }
    }

    fun countEntriesAfterTime(time: TimeOfDay): Int {
        return entries.count { it.time > time }
    }

    fun countEntriesBetweenTimes(minTime: TimeOfDay, maxTime: TimeOfDay): Int {
        return entries.count { it.time in minTime..maxTime }
    }
}

@Composable
fun WeekGridChart(
    columns: ArrayList<WeekGridChartColumn>,
    minTimeOfDay: TimeOfDay,
    maxTimeOfDay: TimeOfDay,
    canvasModifier: Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val chartGuideLineColor = colorResource(R.color.chart_guide_lines)
    val replyToColor = mapOf(
        PostureCheckReply.GOOD to colorResource(R.color.accent_yellow),
        PostureCheckReply.BAD to colorResource(R.color.dark_green),
        PostureCheckReply.NOT_APPLICABLE to colorResource(R.color.light_mint),
        PostureCheckReply.NO_ANSWER to colorResource(R.color.light_mint),
    )
    Canvas(canvasModifier) {
        val minTimeOfDayText = minTimeOfDay.toString()
        val maxTimeOfDayText = maxTimeOfDay.toString()
        val textStyle = h4.copy(color = chartGuideLineColor)
        val minTimeOfDayTextMeasurement = textMeasurer.measure(minTimeOfDayText, textStyle)
        val maxTimeOfDayTextMeasurement = textMeasurer.measure(maxTimeOfDayText, textStyle)
        val yAxisLabelsWidth = max(
            minTimeOfDayTextMeasurement.size.width.toFloat(),
            maxTimeOfDayTextMeasurement.size.width.toFloat(),
        )
        val xAxisLabelsHeight = size.height * 0.15f
        val maxEntriesBeforeMinTime = columns.maxOf { it.countEntriesBeforeTime(minTimeOfDay) }
        val maxEntriesAfterMaxTime = columns.maxOf { it.countEntriesAfterTime(maxTimeOfDay) }
        val maxEntriesBetweenLines = columns.maxOf { it.countEntriesBetweenTimes(minTimeOfDay, maxTimeOfDay) }

        // Add some padding on the right so that the last label can fit.
        val rightPadding = textMeasurer.measure(columns.last().label, textStyle).size.width / 2f

        val chartWidth = size.width - yAxisLabelsWidth - rightPadding
        val chartHeight = size.height - xAxisLabelsHeight
        val columnWidth = chartWidth / columns.size

        val maxCircleSize = min(columnWidth, chartHeight / (maxEntriesBeforeMinTime + maxEntriesBetweenLines + maxEntriesAfterMaxTime))

        val firstLineY = maxEntriesBeforeMinTime * maxCircleSize
        val secondLineY = (maxEntriesBeforeMinTime + maxEntriesBetweenLines) * maxCircleSize

        // Draw Y Axis.
        inset(
            left = 0f,
            top = 0f,
            right = size.width - yAxisLabelsWidth,
            bottom = 0f
        ) {
            drawText(
                text = minTimeOfDayText,
                textMeasurer = textMeasurer,
                style = textStyle,
                topLeft = Offset(0f, firstLineY - minTimeOfDayTextMeasurement.size.height / 2)
            )
            drawText(
                text = maxTimeOfDayText,
                textMeasurer = textMeasurer,
                style = textStyle,
                topLeft = Offset(0f, secondLineY - maxTimeOfDayTextMeasurement.size.height / 2)
            )
        }

        // Draw X Axis
        inset(
            left = yAxisLabelsWidth,
            right = rightPadding,
            top = size.height - xAxisLabelsHeight,
            bottom = 0f
        ) {
            for (i in 0..<columns.size) {
                val column = columns[i]
                val labelTextMeasurement = textMeasurer.measure(column.label, textStyle)
                val x = i * columnWidth + columnWidth / 2
                drawText(
                    text = column.label,
                    textMeasurer = textMeasurer,
                    style = textStyle,
                    topLeft = Offset(
                        x - labelTextMeasurement.size.width / 2,
                        0f
                    )
                )
            }
        }

        // Draw chart.
        inset(
            left = yAxisLabelsWidth,
            right = rightPadding,
            top = 0f,
            bottom = xAxisLabelsHeight
        ) {
            drawLine(
                color = chartGuideLineColor,
                start = Offset(0f, firstLineY),
                end = Offset(size.width, firstLineY)
            )
            drawLine(
                color = chartGuideLineColor,
                start = Offset(0f, secondLineY),
                end = Offset(size.width, secondLineY)
            )
            for (colIdx in 0..<columns.size) {
                val column = columns[colIdx]
                val x = colIdx * columnWidth + columnWidth / 2
                // TODO: Dedup the code below!
                // First draw entries before first guide line.
                var entries = column.entriesBeforeTime(minTimeOfDay)
                var startOffset = (maxEntriesBeforeMinTime - column.countEntriesBeforeTime(minTimeOfDay)) * maxCircleSize
                for (entryIdx in 0..<entries.size) {
                    val entry = entries[entryIdx]
                    val y = startOffset + entryIdx * maxCircleSize
                    drawCircle(
                        color = replyToColor[entry.reply]!!,
                        radius = maxCircleSize * 0.45f,
                        center = Offset(x, y + maxCircleSize / 2)
                    )
                }

                // Now draw entries between the guide lines.
                entries = column.entriesBetweenTimes(minTimeOfDay, maxTimeOfDay)
                startOffset = maxEntriesBeforeMinTime * maxCircleSize
                for (entryIdx in 0..<entries.size) {
                    val entry = entries[entryIdx]
                    val y = startOffset + entryIdx * maxCircleSize
                    drawCircle(
                        color = replyToColor[entry.reply]!!,
                        radius = maxCircleSize * 0.45f,
                        center = Offset(x, y + maxCircleSize / 2)
                    )
                }

                // Finally, draw entries after the second guide line.
                entries = column.entriesAfterTime(maxTimeOfDay)
                startOffset = (maxEntriesBeforeMinTime + maxEntriesBetweenLines) * maxCircleSize
                for (entryIdx in 0..<entries.size) {
                    val entry = entries[entryIdx]
                    val y = startOffset + entryIdx * maxCircleSize
                    drawCircle(
                        color = replyToColor[entry.reply]!!,
                        radius = maxCircleSize * 0.45f,
                        center = Offset(x, y + maxCircleSize / 2)
                    )
                }
            }
        }
    }
}