package com.tgarbus.posturecheck.ui.reusables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4

data class LineChartEntry(
    val value: Float,  // between 0 and 1
    val label: String
)

@Composable
fun LineChart(
    entries: ArrayList<LineChartEntry>,
    canvasModifier: Modifier = Modifier
) {
    val chartGuideLineColor = colorResource(R.color.chart_guide_lines)
    val lineChartColor = colorResource(R.color.dark_green)
    val gradientColors = listOf(
        colorResource(R.color.mint),
        Color.Transparent,
    )
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = canvasModifier
    ) {
        Log.d("tomek", "size1 ${size}")

        val yesText = "Yes"
        val noText = "No"
        val textStyle = h4.copy(color = chartGuideLineColor)
        val yesTextMeasurement = textMeasurer.measure(yesText, textStyle)
        val noTextMeasurement = textMeasurer.measure(noText, textStyle)

        val yAxisLabelsWidth = yesTextMeasurement.size.width.toFloat()
        val xAxisLabelsHeight = size.height * 0.15f

        inset(left = 0f, top = 0f, right = size.width - yAxisLabelsWidth, bottom = xAxisLabelsHeight) {
            drawText(
                textMeasurer = textMeasurer,
                style = textStyle,
                topLeft = Offset(
                    0f,
                    0f - yesTextMeasurement.size.height / 2,
                ),
                text = yesText
            )
            drawText(
                textMeasurer = textMeasurer,
                style = textStyle,
                topLeft = Offset(
                    0f,
                    size.height / 2 - noTextMeasurement.size.height / 2,
                ),
                text = noText
            )
        }
        inset(left = yAxisLabelsWidth, top = 0f, right = 0f, bottom = xAxisLabelsHeight) {
            drawLine(
                color = chartGuideLineColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f)
            )
            drawLine(
                color = chartGuideLineColor,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2)
            )
            drawLine(
                color = chartGuideLineColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height)
            )
            val entryValueToOffset: (value: Float, idx: Int) -> Offset = {
                    value, idx ->
                Offset(
                    x = (idx.toFloat() / (entries.size - 1)) * size.width,
                    y = (size.height / 2) * value
                )
            }
            val path = Path()
            val startX = entryValueToOffset(entries[0].value, 0).x
            val startY = entryValueToOffset(entries[0].value, 0).y
            var curX = startX
            var curY = startY
            path.moveTo(curX, curY)
            for (i in 1..<entries.size) {
                val nextX = entryValueToOffset(entries[i].value, i).x
                val nextY = entryValueToOffset(entries[i].value, i).y
                path.cubicTo(
                    (curX + nextX) / 2, curY,
                    (curX + nextX) / 2, nextY,
                    nextX, nextY)
                curX = nextX
                curY = nextY
            }
            // Close the loop.
            val closedPath = path.copy()
            closedPath.lineTo(size.width, size.height)
            closedPath.lineTo(0f, size.height)
            closedPath.lineTo(startX, startY)
            drawPath(
                closedPath,
                brush = Brush.verticalGradient(gradientColors, startY = 0f, endY = size.height)
            )
            drawPath(
                path,
                color = lineChartColor,
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
            )
        }
        inset(left = yAxisLabelsWidth, top = size.height - xAxisLabelsHeight, right = 0f, bottom = 0f) {
                for (i in 0..<entries.size) {
                    val entry = entries[i]
                    val textMeasurement = textMeasurer.measure(entry.label, textStyle)
                    val offset = Offset(
                        x = (i.toFloat() / (entries.size - 1)) * size.width,
                        y = size.height / 2
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        style = textStyle,
                        topLeft = Offset(
                            offset.x - textMeasurement.size.width / 2,
                            offset.y - textMeasurement.size.height / 2
                        ),
                        text = entry.label
                    )
                }
            }
    }
}