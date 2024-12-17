package com.tgarbus.posturecheck.ui.reusables

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import kotlin.math.min

data class CircularGraphComponent(
    val percentage: Int,
    val color: Color,
)

@Composable
fun CircularGraph(
    entries: List<CircularGraphComponent>,
    startingAngle: Float = 240f,
    canvasModifier: Modifier = Modifier
) {
    // val textMeasurer = rememberTextMeasurer()
    val bufferAngle = 20f
    val strokeWidth = 50f
    val circleMinusBuffers = 360f - bufferAngle * entries.size
    Canvas(
        modifier = canvasModifier
    ) {
        Log.d("tomek", "size1 ${size}")
        val radius = min(size.width / 2, size.height) - strokeWidth / 2
        inset(
            size.width / 2 - radius,
            size.height / 2 - radius) {
            Log.d("tomek", "size2 ${size}")
            var sumAngles = startingAngle
            for (entry in entries) {
                val sweepAngle = circleMinusBuffers * (entry.percentage / 100f)
                drawArc(
                    startAngle = sumAngles,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    color = entry.color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                sumAngles += sweepAngle + bufferAngle
            }
            /* val textStyle = h3.copy(color = Color.White, fontSize = 24.sp)
            val measurement = textMeasurer.measure(text, textStyle)
            val topLeft = Offset(
                drawContext.size.center.x - measurement.size.width / 2,
                (drawContext.size.height - measurement.size.height) / 2,
            )
            drawText(
                textMeasurer = textMeasurer,
                style = textStyle,
                topLeft = topLeft,
                text = text
            ) */
        }
    }
}