package com.ablauncher.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ablauncher.data.model.ClockFace
import com.ablauncher.data.model.ClockFormat
import com.ablauncher.ui.components.FrostedGlassPanel
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockWidget(
    face: ClockFace,
    format: ClockFormat,
    modifier: Modifier = Modifier
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            now = LocalDateTime.now()
        }
    }

    FrostedGlassPanel(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            when (face) {
                ClockFace.DIGITAL_SIMPLE -> DigitalSimple(now, format)
                ClockFace.DIGITAL_FULL -> DigitalFull(now, format)
                ClockFace.ANALOG_MINIMAL -> AnalogClock(now, showTicks = false, showNumerals = false)
                ClockFace.ANALOG_CLASSIC -> AnalogClock(now, showTicks = true, showNumerals = true)
            }
        }
    }
}

@Composable
private fun DigitalSimple(now: LocalDateTime, format: ClockFormat) {
    val timeStr = if (format == ClockFormat.HOUR_12) {
        now.format(DateTimeFormatter.ofPattern("hh:mm a"))
    } else {
        now.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    Text(
        text = timeStr,
        fontSize = 64.sp,
        fontWeight = FontWeight.Light,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun DigitalFull(now: LocalDateTime, format: ClockFormat) {
    val timeStr = if (format == ClockFormat.HOUR_12) {
        now.format(DateTimeFormatter.ofPattern("hh:mm"))
    } else {
        now.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    val amPm = if (format == ClockFormat.HOUR_12) now.format(DateTimeFormatter.ofPattern("a")) else ""
    val dateStr = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = timeStr + if (amPm.isNotEmpty()) " $amPm" else "",
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = dateStr,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnalogClock(
    now: LocalDateTime,
    showTicks: Boolean,
    showNumerals: Boolean
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(140.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f - 4.dp.toPx()

        // Circle outline
        drawCircle(
            color = onSurface.copy(alpha = 0.3f),
            radius = radius,
            center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )

        if (showTicks) {
            for (i in 0 until 12) {
                val angle = Math.toRadians(i * 30.0 - 90.0)
                val innerR = radius - 10.dp.toPx()
                val outerR = radius - 4.dp.toPx()
                drawLine(
                    color = onSurface.copy(alpha = 0.6f),
                    start = Offset(
                        cx + (innerR * cos(angle)).toFloat(),
                        cy + (innerR * sin(angle)).toFloat()
                    ),
                    end = Offset(
                        cx + (outerR * cos(angle)).toFloat(),
                        cy + (outerR * sin(angle)).toFloat()
                    ),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }

        // Hour hand
        val hourAngle = ((now.hour % 12) * 30f + now.minute * 0.5f)
        drawHand(cx, cy, hourAngle, radius * 0.55f, onSurface, 4.dp.toPx())

        // Minute hand
        val minAngle = now.minute * 6f
        drawHand(cx, cy, minAngle, radius * 0.78f, onSurface, 2.5.dp.toPx())

        // Second hand
        val secAngle = now.second * 6f
        drawHand(cx, cy, secAngle, radius * 0.85f, primary, 1.5.dp.toPx())

        // Center dot
        drawCircle(color = onSurface, radius = 4.dp.toPx(), center = Offset(cx, cy))
    }
}

private fun DrawScope.drawHand(
    cx: Float, cy: Float,
    angleDeg: Float,
    length: Float,
    color: Color,
    strokeWidth: Float
) {
    val angle = Math.toRadians((angleDeg - 90.0))
    drawLine(
        color = color,
        start = Offset(cx, cy),
        end = Offset(
            cx + (length * cos(angle)).toFloat(),
            cy + (length * sin(angle)).toFloat()
        ),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
