package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.InstrumentGreen
import com.example.ui.theme.InstrumentOrange
import com.example.ui.theme.InstrumentRed
import com.example.ui.theme.InstrumentYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogSpeedometerGauge(
    currentSpeed: Float,
    maxSpeed: Float,
    unitLabel: String,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    // Clamp speed to gauge scale
    val clampedSpeed = currentSpeed.coerceIn(0f, maxSpeed)
    // Animate needle smoothly with spring damping
    val animatedSpeed by animateFloatAsState(
        targetValue = clampedSpeed,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 120f),
        label = "SpeedNeedleAnimation"
    )

    // Gauge sweep angles: 240 degrees sweep total, starting at 150° (bottom-left) to 390° (30° bottom-right)
    val startAngle = 140f
    val sweepAngle = 260f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("analog_speedometer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 12.dp.toPx()

            // 1. Outer Dark Dial Background Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF141E2B), Color(0xFF090D12)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Outer subtle border
            drawCircle(
                color = DarkSurfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // 2. Color-coded Arc Track (Green -> Yellow -> Red)
            val strokeWidth = 10.dp.toPx()
            val arcRadius = radius - strokeWidth / 2f - 4.dp.toPx()

            // Safe range arc (0 - 50% max speed)
            drawArc(
                color = InstrumentGreen.copy(alpha = 0.85f),
                startAngle = startAngle,
                sweepAngle = sweepAngle * 0.5f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Moderate range arc (50% - 80% max speed)
            drawArc(
                color = InstrumentYellow.copy(alpha = 0.85f),
                startAngle = startAngle + sweepAngle * 0.5f,
                sweepAngle = sweepAngle * 0.3f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = strokeWidth)
            )

            // Fast / Warning range arc (80% - 100% max speed)
            drawArc(
                color = InstrumentRed.copy(alpha = 0.85f),
                startAngle = startAngle + sweepAngle * 0.8f,
                sweepAngle = sweepAngle * 0.2f,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Tick Marks and Numeric Labels
            val tickCount = 10 // major ticks
            val tickRadiusOuter = radius - strokeWidth - 8.dp.toPx()
            val majorTickLength = 12.dp.toPx()
            val minorTickLength = 6.dp.toPx()

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 12.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            }

            // Draw 40 ticks total (10 major, 3 minor between each)
            val totalTicks = tickCount * 4
            for (i in 0..totalTicks) {
                val fraction = i.toFloat() / totalTicks
                val angleDeg = startAngle + fraction * sweepAngle
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val isMajor = i % 4 == 0
                val tickLength = if (isMajor) majorTickLength else minorTickLength
                val tickWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
                val tickColor = if (isMajor) Color.White else Color(0xFF64748B)

                val startX = center.x + tickRadiusOuter * cos(angleRad).toFloat()
                val startY = center.y + tickRadiusOuter * sin(angleRad).toFloat()
                val endX = center.x + (tickRadiusOuter - tickLength) * cos(angleRad).toFloat()
                val endY = center.y + (tickRadiusOuter - tickLength) * sin(angleRad).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )

                // Numeric label for major ticks
                if (isMajor) {
                    val labelValue = (fraction * maxSpeed).toInt()
                    val labelRadius = tickRadiusOuter - tickLength - 12.dp.toPx()
                    val labelX = center.x + labelRadius * cos(angleRad).toFloat()
                    val labelY = center.y + labelRadius * sin(angleRad).toFloat() + 4.dp.toPx()

                    drawContext.canvas.nativeCanvas.drawText(
                        labelValue.toString(),
                        labelX,
                        labelY,
                        textPaint
                    )
                }
            }

            // 4. Animated Speed Needle
            val currentFraction = (animatedSpeed / maxSpeed).coerceIn(0f, 1f)
            val needleAngleDeg = startAngle + currentFraction * sweepAngle
            val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleLength = radius - 18.dp.toPx()
            val needleBaseWidth = 6.dp.toPx()

            // Needle polygon path (tapered blade with sharp tip)
            val tipX = center.x + needleLength * cos(needleAngleRad).toFloat()
            val tipY = center.y + needleLength * sin(needleAngleRad).toFloat()

            val perpAngleRad = needleAngleRad + Math.PI / 2
            val base1X = center.x + needleBaseWidth * cos(perpAngleRad).toFloat()
            val base1Y = center.y + needleBaseWidth * sin(perpAngleRad).toFloat()
            val base2X = center.x - needleBaseWidth * cos(perpAngleRad).toFloat()
            val base2Y = center.y - needleBaseWidth * sin(perpAngleRad).toFloat()

            // Tail behind pivot
            val tailLength = 16.dp.toPx()
            val tailX = center.x - tailLength * cos(needleAngleRad).toFloat()
            val tailY = center.y - tailLength * sin(needleAngleRad).toFloat()

            val needlePath = Path().apply {
                moveTo(tipX, tipY)
                lineTo(base1X, base1Y)
                lineTo(tailX, tailY)
                lineTo(base2X, base2Y)
                close()
            }

            val needleColor = if (isWarning) InstrumentRed else InstrumentOrange
            drawPath(
                path = needlePath,
                color = needleColor
            )

            // Inner needle highlight line
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = center,
                end = Offset(tipX, tipY),
                strokeWidth = 1.5.dp.toPx()
            )

            // 5. Center Hub Cap
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 16.dp.toPx(),
                center = center
            )
            drawCircle(
                color = needleColor,
                radius = 9.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )
        }

        // Center Digital Readout in lower-half of dial
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format(Locale.US, "%.0f", currentSpeed),
                color = if (isWarning) InstrumentRed else TextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
            )
            Text(
                text = unitLabel,
                color = if (isWarning) InstrumentRed else InstrumentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
