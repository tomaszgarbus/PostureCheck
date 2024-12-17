package com.tgarbus.posturecheck.ui.reusables

import android.graphics.DashPathEffect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.times
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.toSize
import com.tgarbus.posturecheck.R
import com.tgarbus.posturecheck.ui.TextStyles.Companion.h4
import kotlin.math.pow

data class LineChartEntry(
    val value: Float,  // between 0 and 1
    val label: String
)

fun interpolateCubicBezierAtX(
    p0: Offset,
    p1: Offset,
    p2: Offset,
    p3: Offset,
    x: Float,
): Offset {
    assert(p3.x > p0.x)
    val t = (x - p0.x) / (p3.x - p0.x)
    Log.d("tomek", "t: ${t} ${p0 * (1f - t).pow(3f)} ${p0}")
    return (p0 * (1f - t).pow(3f)
            + p1 * 3f * t * (1f - t).pow(2f)
            + p2 * 3f * t.pow(2) * (1f - t)
            + p3 * t.pow(3))
}

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
    val rectTooltipColor = colorResource(R.color.light_mint)
    val textMeasurer = rememberTextMeasurer()
    val touchOffset = remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = canvasModifier.pointerInput("LineChart") {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    Log.d("tomek", "${event.type} ${event.changes}")
                    if (event.type == PointerEventType.Release) {
                        touchOffset.value = null
                    }
                    if (event.type == PointerEventType.Press || event.type == PointerEventType.Move) {
                        touchOffset.value = event.changes.first().position
                    }
                }
            }
        }
    ) {
        Log.d("tomek", "size1 ${size}")

        val yesText = "Yes"
        val noText = "No"
        val textStyle = h4.copy(color = chartGuideLineColor)
        val yesTextMeasurement = textMeasurer.measure(yesText, textStyle)
        val noTextMeasurement = textMeasurer.measure(noText, textStyle)

        val yAxisLabelsWidth = yesTextMeasurement.size.width.toFloat()
        val xAxisLabelsHeight = size.height * 0.15f

        // Add some padding on the right so that the last label can fit.
        val rightPadding = textMeasurer.measure(entries.last().label, textStyle).size.width / 2f

        // Y Axis labels.
        inset(left = 0f, top = 0f, right = size.width - yAxisLabelsWidth - rightPadding, bottom = xAxisLabelsHeight) {
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
        // Chart
        inset(left = yAxisLabelsWidth, top = 0f, right = rightPadding, bottom = xAxisLabelsHeight) {
            // Draw guiding lines.
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
            // Map entries to coordinate points through which we guide the Bezier curves (ends of
            // Bezier curves to be exact).
            val entryValueToOffset: (value: Float, idx: Int) -> Offset = {
                    value, idx ->
                Offset(
                    x = (idx.toFloat() / (entries.size - 1)) * size.width,
                    y = (size.height / 2) * value
                )
            }
            // Draw the path.
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
            // Close the loop and fill with gradient.
            val closedPath = path.copy()
            closedPath.lineTo(size.width, size.height)
            closedPath.lineTo(0f, size.height)
            closedPath.lineTo(startX, startY)
            drawPath(
                closedPath,
                brush = Brush.verticalGradient(gradientColors, startY = 0f, endY = size.height)
            )
            // Finally, stroke the path.
            drawPath(
                path,
                color = lineChartColor,
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                )
            )

            if (
                touchOffset.value != null
                && touchOffset.value!!.x >= yAxisLabelsWidth
                && touchOffset.value!!.x - yAxisLabelsWidth <= size.width) {
                val touchX = touchOffset.value!!.x - yAxisLabelsWidth
                // Find the closest entry index on the left.
                var previousEntryIndex = 0
                while (
                    previousEntryIndex + 1 < entries.size
                    && entryValueToOffset(entries[previousEntryIndex].value, previousEntryIndex + 1).x < touchX) {
                    previousEntryIndex++
                }
                // Determine touchY at the touchX.
                val leftPointOffset = entryValueToOffset(
                    entries[previousEntryIndex].value, previousEntryIndex)
                var touchY = leftPointOffset.y
                if (previousEntryIndex + 1 < entries.size) {
                    val rightPointOffset = entryValueToOffset(
                        entries[previousEntryIndex + 1].value, previousEntryIndex + 1)
                    touchY = interpolateCubicBezierAtX(
                        p0 = leftPointOffset,
                        p1 = Offset((leftPointOffset.x + rightPointOffset.x) / 2, leftPointOffset.y),
                        p2 = Offset((leftPointOffset.x + rightPointOffset.x) / 2, rightPointOffset.y),
                        p3 = rightPointOffset,
                        x = touchX
                    ).y
                }
                // Draw the vertical line.
                drawLine(
                    color = lineChartColor,
                    start = Offset(x = touchX, 0f),
                    end = Offset(x = touchX, size.height),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    strokeWidth = 5f,
                )
                drawCircle(
                    color = Color.White,
                    center = Offset(touchX, touchY),
                    radius = 10f,
                )
                drawCircle(
                    color = lineChartColor,
                    center = Offset(touchX, touchY),
                    radius = 10f,
                    style = Stroke(
                        width = 5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    )
                )
                // Draw Round Rectangle to the left or right of the moving line.
                val popupText = "80% straight"
                val textMeasuredSize = textMeasurer.measure(popupText, textStyle).size.toSize()
                val rectPad = 10f
                val rectSize = Size(
                    textMeasuredSize.width + rectPad * 2,
                    textMeasuredSize.height + rectPad * 2)
                var rectTopLeft = Offset(x = touchX + 10f, y = touchY)
                if (rectTopLeft.x + textMeasuredSize.width + 10f >= size.width) {
                    rectTopLeft = Offset(
                        x = touchX - rectSize.width - 10f,
                        y = touchY
                    )
                }
                drawRoundRect(
                    color = rectTooltipColor,
                    topLeft = rectTopLeft,
                    cornerRadius = CornerRadius(17f, 17f),
                    size = rectSize
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = popupText,
                    style = textStyle.copy(color = lineChartColor),
                    topLeft = rectTopLeft + Offset(rectPad, rectPad),
                )
            }
        }
        // X Axis labels
        inset(left = yAxisLabelsWidth, top = size.height - xAxisLabelsHeight, right = 0f, bottom = 0f) {
                for (i in 0..<entries.size) {
                    val entry = entries[i]
                    val textMeasurement = textMeasurer.measure(entry.label, textStyle)
                    val offset = Offset(
                        x = (i.toFloat() / (entries.size - 1)) * (size.width - rightPadding),
                        y = size.height / 2
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        style = textStyle,
                        overflow = TextOverflow.Visible,
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