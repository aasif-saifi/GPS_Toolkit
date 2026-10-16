package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.InstrumentGreen
import com.example.ui.theme.InstrumentRed
import com.example.ui.theme.InstrumentYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalCompassDial(
    azimuthDegrees: Float,
    pitchDegrees: Float,
    rollDegrees: Float,
    modifier: Modifier = Modifier,
    showCenterBubble: Boolean = true
) {
    // Smooth animated rotation of the compass dial
    val animatedAzimuth by animateFloatAsState(
        targetValue = azimuthDegrees,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 140f),
        label = "CompassAzimuthAnimation"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("tactical_compass_dial"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 6.dp.toPx()

            // 1. Outer Dark Bezel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF16212E), Color(0xFF0A0F15)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = DarkSurfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 2. Rotating Compass Disc
            // The dial rotates counter-clockwise by azimuth so North points toward the physical world
            rotate(degrees = -animatedAzimuth, pivot = center) {
                val tickRadiusOuter = radius - 6.dp.toPx()

                // Tick marks: 72 ticks (every 5 degrees)
                for (deg in 0 until 360 step 5) {
                    val isMajor = deg % 30 == 0
                    val isMedium = deg % 10 == 0 && !isMajor
                    val tickLength = when {
                        isMajor -> 14.dp.toPx()
                        isMedium -> 8.dp.toPx()
                        else -> 4.dp.toPx()
                    }
                    val tickWidth = when {
                        isMajor -> 2.dp.toPx()
                        isMedium -> 1.5.dp.toPx()
                        else -> 1.dp.toPx()
                    }
                    val tickColor = when {
                        deg == 0 -> InstrumentRed
                        isMajor -> Color.White
                        else -> Color(0xFF6B7D91)
                    }

                    val rad = Math.toRadians(deg.toDouble() - 90.0)
                    val startX = center.x + tickRadiusOuter * cos(rad).toFloat()
                    val startY = center.y + tickRadiusOuter * sin(rad).toFloat()
                    val endX = center.x + (tickRadiusOuter - tickLength) * cos(rad).toFloat()
                    val endY = center.y + (tickRadiusOuter - tickLength) * sin(rad).toFloat()

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickWidth,
                        cap = StrokeCap.Round
                    )

                    // Cardinal & Degree Labels
                    val textPaint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                    }

                    if (deg % 45 == 0) {
                        // Cardinal labels: N, NE, E, SE, S, SW, W, NW
                        val cardinal = when (deg) {
                            0 -> "N"
                            45 -> "NE"
                            90 -> "E"
                            135 -> "SE"
                            180 -> "S"
                            225 -> "SW"
                            270 -> "W"
                            315 -> "NW"
                            else -> ""
                        }

                        val cardinalRadius = tickRadiusOuter - 24.dp.toPx()
                        val cx = center.x + cardinalRadius * cos(rad).toFloat()
                        val cy = center.y + cardinalRadius * sin(rad).toFloat() + 5.dp.toPx()

                        textPaint.textSize = if (cardinal == "N") 18.sp.toPx() else 14.sp.toPx()
                        textPaint.color = if (cardinal == "N") android.graphics.Color.parseColor("#FF1744") else android.graphics.Color.WHITE

                        drawContext.canvas.nativeCanvas.drawText(cardinal, cx, cy, textPaint)
                    } else if (isMajor) {
                        // Degree numbers (30, 60, 120, etc.)
                        val degreeRadius = tickRadiusOuter - 22.dp.toPx()
                        val dx = center.x + degreeRadius * cos(rad).toFloat()
                        val dy = center.y + degreeRadius * sin(rad).toFloat() + 4.dp.toPx()

                        textPaint.textSize = 10.sp.toPx()
                        textPaint.color = android.graphics.Color.parseColor("#90A4AE")

                        drawContext.canvas.nativeCanvas.drawText(deg.toString(), dx, dy, textPaint)
                    }
                }

                // North Arrow Triangle indicator
                val northArrowRadius = tickRadiusOuter - 4.dp.toPx()
                val northPath = Path().apply {
                    val tipX = center.x
                    val tipY = center.y - northArrowRadius
                    val leftX = center.x - 8.dp.toPx()
                    val leftY = center.y - northArrowRadius + 14.dp.toPx()
                    val rightX = center.x + 8.dp.toPx()
                    val rightY = center.y - northArrowRadius + 14.dp.toPx()
                    moveTo(tipX, tipY)
                    lineTo(leftX, leftY)
                    lineTo(rightX, rightY)
                    close()
                }
                drawPath(path = northPath, color = InstrumentRed)
            }

            // 3. Center Bullseye Bubble Level (matching compass.jpg)
            if (showCenterBubble) {
                val centerRadius = radius * 0.44f

                // Inner Bullseye background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F2634), Color(0xFF09141D)),
                        center = center,
                        radius = centerRadius
                    ),
                    radius = centerRadius,
                    center = center
                )
                drawCircle(
                    color = InstrumentCyan.copy(alpha = 0.35f),
                    radius = centerRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Concentric reference rings
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = centerRadius * 0.5f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.2f),
                    radius = centerRadius * 0.25f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Crosshair lines
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(center.x - centerRadius, center.y),
                    end = Offset(center.x + centerRadius, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(center.x, center.y - centerRadius),
                    end = Offset(center.x, center.y + centerRadius),
                    strokeWidth = 1.dp.toPx()
                )

                // Bubble position calculated from pitch and roll (clamped to centerRadius)
                val bubbleMaxTravel = centerRadius - 10.dp.toPx()
                val bubbleOffsetX = (rollDegrees / 45f).coerceIn(-1f, 1f) * bubbleMaxTravel
                val bubbleOffsetY = (-pitchDegrees / 45f).coerceIn(-1f, 1f) * bubbleMaxTravel
                val bubbleCenter = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)

                val isLevel = kotlin.math.abs(pitchDegrees) <= 1.5f && kotlin.math.abs(rollDegrees) <= 1.5f
                val bubbleColor = if (isLevel) InstrumentGreen else InstrumentYellow

                // Bubble shadow and body
                drawCircle(
                    color = bubbleColor.copy(alpha = 0.4f),
                    radius = 9.dp.toPx(),
                    center = bubbleCenter
                )
                drawCircle(
                    color = bubbleColor,
                    radius = 6.dp.toPx(),
                    center = bubbleCenter
                )

                // Central crosshair marker (+)
                val crossSize = 4.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(center.x - crossSize, center.y),
                    end = Offset(center.x + crossSize, center.y),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(center.x, center.y - crossSize),
                    end = Offset(center.x, center.y + crossSize),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // 4. Top Reference Cursor (Static 12 o'clock pointer line)
            val topIndicatorLength = 16.dp.toPx()
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - radius - 2.dp.toPx()),
                end = Offset(center.x, center.y - radius + topIndicatorLength),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
