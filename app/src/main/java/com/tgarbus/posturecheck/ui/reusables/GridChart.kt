package com.tgarbus.posturecheck.ui.reusables

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.data.Day
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

data class GridChartEntry(
    val color: Color
)

data class GridChartHorizontalLineSpec(
    val afterNumRows: Int,
    val label: String,
)

data class GridChartSpec(
    val numRows: Int,
    val numColumns: Int,
    val entries: ArrayList<ArrayList<GridChartEntry?>>,  // Indexed entries[column][row]
    val columnLabels: ArrayList<String?>,
    val horizontalLineSpecs: ArrayList<GridChartHorizontalLineSpec>,
)

@Composable
fun GridChartOnCanvas(
    spec: GridChartSpec,
    canvasModifier: Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val chartGuideLineColor = colorResource(R.color.chart_guide_lines)
    val textStyle = h4.copy(color = chartGuideLineColor)
    Canvas(canvasModifier) {
        val yAxisLabelsWidth = spec.horizontalLineSpecs.maxOf {
            textMeasurer.measure(it.label, textStyle).size.width
        }.toFloat()
        val rightPadding = textMeasurer.measure(spec.columnLabels.last() ?: "", textStyle).size.width.toFloat()
        val xAxisLabelsHeight = max(
            size.height * 0.15f,
            spec.columnLabels.maxOf {
                textMeasurer.measure(it ?: "", textStyle).size.height
            }.toFloat()
        )
        val chartWidth = size.width - yAxisLabelsWidth - rightPadding
        val chartHeight = size.height - xAxisLabelsHeight
        val columnWidth = chartWidth / spec.numColumns
        val rowHeight = chartHeight / spec.numRows
        val maxCircleSize = min(columnWidth, rowHeight)

        // Draw Y Axis.
        inset(
            left = 0f,
            top = 0f,
            right = size.width - yAxisLabelsWidth,
            bottom = 0f
        ) {
            // Draw horizontal guide lines' labels.
            for (hls in spec.horizontalLineSpecs) {
                drawText(
                    text = hls.label,
                    textMeasurer = textMeasurer,
                    style = textStyle,
                    topLeft = Offset(0f, hls.afterNumRows * rowHeight - textMeasurer.measure(hls.label, textStyle).size.height / 2)
                )
            }
        }

        // Draw X Axis.
        inset(
            left = yAxisLabelsWidth,
            right = rightPadding,
            top = size.height - xAxisLabelsHeight,
            bottom = 0f
        ) {
            for (i in 0..<spec.numColumns) {
                val label = spec.columnLabels[i] ?: continue
                drawText(
                    text = label,
                    textMeasurer = textMeasurer,
                    style = textStyle,
                    topLeft = Offset(
                        i * columnWidth + columnWidth / 2 - textMeasurer.measure(label, textStyle).size.width / 2,
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
            // Draw guide lines.
            for (hls in spec.horizontalLineSpecs) {
                drawLine(
                    color = chartGuideLineColor,
                    start = Offset(0f, hls.afterNumRows * rowHeight),
                    end = Offset(size.width, hls.afterNumRows * rowHeight),
                )
            }

            // Now draw the circles.
            for (row in 0..<spec.numRows) {
                for (col in 0..<spec.numColumns) {
                    val entry = spec.entries[col][row] ?: continue
                    val x = columnWidth * (col + 0.5f)
                    val y = rowHeight * (row + 0.5f)
                    drawCircle(
                        color = entry.color,
                        radius = maxCircleSize * 0.45f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

@Composable
fun WeekGridChart(
    columns: ArrayList<WeekGridChartColumn>,
    minTimeOfDay: TimeOfDay,
    maxTimeOfDay: TimeOfDay,
    canvasModifier: Modifier) {
    val maxEntriesBeforeMinTime = columns.maxOf { it.countEntriesBeforeTime(minTimeOfDay) }
    val maxEntriesAfterMaxTime = columns.maxOf { it.countEntriesAfterTime(maxTimeOfDay) }
    val maxEntriesBetweenLines = columns.maxOf { it.countEntriesBetweenTimes(minTimeOfDay, maxTimeOfDay) }
    val numRows = maxEntriesBeforeMinTime + maxEntriesBetweenLines + maxEntriesAfterMaxTime
    val numColumns = columns.size
    val horizontalLineSpecs = arrayListOf(
        GridChartHorizontalLineSpec(
            afterNumRows = maxEntriesBeforeMinTime,
            label = minTimeOfDay.toString()
        ),
        GridChartHorizontalLineSpec(
            afterNumRows = maxEntriesBeforeMinTime + maxEntriesBetweenLines,
            label = maxTimeOfDay.toString()
        ),
    )
    val columnLabels = ArrayList(columns.map { it.label })
    val entries: ArrayList<ArrayList<GridChartEntry?>> = ArrayList((0..<numColumns).map {
        ArrayList((0..<numRows).map { null })
    })
    val replyToColor = mapOf(
        PostureCheckReply.GOOD to colorResource(R.color.accent_yellow),
        PostureCheckReply.BAD to colorResource(R.color.dark_green),
        PostureCheckReply.NOT_APPLICABLE to colorResource(R.color.light_mint),
        PostureCheckReply.NO_ANSWER to colorResource(R.color.light_mint),
    )
    for (col in 0..<numColumns) {
        val entriesBefore = columns[col].entriesBeforeTime(minTimeOfDay)
        for (i in 0..<entriesBefore.size) {
            val row = maxEntriesBeforeMinTime - entriesBefore.size + i
            entries[col][row] = GridChartEntry(color = replyToColor[entriesBefore[i].reply]!!)
        }
        val entriesBetween = columns[col].entriesBetweenTimes(minTimeOfDay, maxTimeOfDay)
        for (i in 0..<entriesBetween.size) {
            val row = maxEntriesBeforeMinTime + i
            entries[col][row] = GridChartEntry(color = replyToColor[entriesBetween[i].reply]!!)
        }
        val entriesAfter = columns[col].entriesAfterTime(maxTimeOfDay)
        for (i in 0..<entriesAfter.size) {
            val row = maxEntriesBeforeMinTime + maxEntriesBetweenLines + i
            entries[col][row] = GridChartEntry(color = replyToColor[entriesAfter[i].reply]!!)
        }
    }
    val spec = GridChartSpec(
        numRows = numRows,
        numColumns = numColumns,
        entries = entries,
        columnLabels = columnLabels,
        horizontalLineSpecs = horizontalLineSpecs,
    )
    GridChartOnCanvas(
        spec,
        canvasModifier
    )
}
