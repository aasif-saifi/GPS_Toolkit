package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.InstrumentGreen
import com.example.ui.theme.InstrumentYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs

@Composable
fun CircularBullseyeLevel(
    pitchDegrees: Float,
    rollDegrees: Float,
    toleranceDegrees: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(pitchDegrees) <= toleranceDegrees && abs(rollDegrees) <= toleranceDegrees

    val animatedColor by animateColorAsState(
        targetValue = if (isLevel) InstrumentGreen else InstrumentYellow,
        label = "BullseyeLevelColor"
    )

    val animatedPitch by animateFloatAsState(
        targetValue = pitchDegrees,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 160f),
        label = "BullseyePitch"
    )
    val animatedRoll by animateFloatAsState(
        targetValue = rollDegrees,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 160f),
        label = "BullseyeRoll"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .testTag("circular_bullseye_level"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension / 2f - 8.dp.toPx()

            // Outer dark container
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF142433), Color(0xFF0A121A)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = if (isLevel) InstrumentGreen else DarkBorder,
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isLevel) 3.dp.toPx() else 1.5.dp.toPx())
            )

            // Outer reference rings (10° and 20° tilt)
            drawCircle(
                color = Color(0xFF2C3E50),
                radius = outerRadius * 0.75f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF2C3E50),
                radius = outerRadius * 0.45f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Inner target tolerance circle
            val targetRadius = outerRadius * 0.22f
            drawCircle(
                color = animatedColor.copy(alpha = 0.25f),
                radius = targetRadius,
                center = center
            )
            drawCircle(
                color = animatedColor,
                radius = targetRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Crosshairs
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(center.x - outerRadius, center.y),
                end = Offset(center.x + outerRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.35f),
                start = Offset(center.x, center.y - outerRadius),
                end = Offset(center.x, center.y + outerRadius),
                strokeWidth = 1.dp.toPx()
            )

            // Floating bubble
            val maxTravel = outerRadius - 16.dp.toPx()
            // Map degrees [-45, 45] to [-maxTravel, maxTravel]
            val bubbleOffsetX = (animatedRoll / 30f).coerceIn(-1f, 1f) * maxTravel
            val bubbleOffsetY = (-animatedPitch / 30f).coerceIn(-1f, 1f) * maxTravel
            val bubbleCenter = Offset(center.x + bubbleOffsetX, center.y + bubbleOffsetY)

            // Bubble body and glow
            drawCircle(
                color = animatedColor.copy(alpha = 0.4f),
                radius = 20.dp.toPx(),
                center = bubbleCenter
            )
            drawCircle(
                color = animatedColor,
                radius = 14.dp.toPx(),
                center = bubbleCenter
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(bubbleCenter.x - 3.dp.toPx(), bubbleCenter.y - 3.dp.toPx())
            )

            // Center target crosshair (+)
            val crossSize = 5.dp.toPx()
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
    }
}

@Composable
fun TubularHorizontalVial(
    rollDegrees: Float,
    toleranceDegrees: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(rollDegrees) <= toleranceDegrees
    val animatedRoll by animateFloatAsState(
        targetValue = rollDegrees.coerceIn(-45f, 45f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 160f),
        label = "TubularRoll"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ROLL (HORIZONTAL)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(
                String.format(Locale.US, "%.1f°", rollDegrees),
                color = if (isLevel) InstrumentGreen else TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(Color(0xFF0F1A24))
                .border(1.5.dp, if (isLevel) InstrumentGreen else DarkBorder, RoundedCornerShape(19.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                val centerY = size.height / 2f
                val centerX = size.width / 2f
                val vialWidth = size.width

                // Center tolerance markings (target zone)
                val targetWindowWidth = 24.dp.toPx()
                drawLine(
                    color = InstrumentGreen,
                    start = Offset(centerX - targetWindowWidth / 2f, 4.dp.toPx()),
                    end = Offset(centerX - targetWindowWidth / 2f, size.height - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = InstrumentGreen,
                    start = Offset(centerX + targetWindowWidth / 2f, 4.dp.toPx()),
                    end = Offset(centerX + targetWindowWidth / 2f, size.height - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )

                // Bubble movement along X
                val maxTravel = (vialWidth / 2f) - 18.dp.toPx()
                val bubbleOffset = (animatedRoll / 30f).coerceIn(-1f, 1f) * maxTravel
                val bubbleCenter = Offset(centerX + bubbleOffset, centerY)
                val bubbleColor = if (isLevel) InstrumentGreen else InstrumentYellow

                drawCircle(
                    color = bubbleColor.copy(alpha = 0.4f),
                    radius = 16.dp.toPx(),
                    center = bubbleCenter
                )
                drawCircle(
                    color = bubbleColor,
                    radius = 11.dp.toPx(),
                    center = bubbleCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(bubbleCenter.x - 2.dp.toPx(), bubbleCenter.y - 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun TubularVerticalVial(
    pitchDegrees: Float,
    toleranceDegrees: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(pitchDegrees) <= toleranceDegrees
    val animatedPitch by animateFloatAsState(
        targetValue = pitchDegrees.coerceIn(-45f, 45f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 160f),
        label = "TubularPitch"
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("PITCH", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(
            String.format(Locale.US, "%.1f°", pitchDegrees),
            color = if (isLevel) InstrumentGreen else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(38.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(19.dp))
                .background(Color(0xFF0F1A24))
                .border(1.5.dp, if (isLevel) InstrumentGreen else DarkBorder, RoundedCornerShape(19.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val vialHeight = size.height

                // Center tolerance markings (target zone)
                val targetWindowHeight = 24.dp.toPx()
                drawLine(
                    color = InstrumentGreen,
                    start = Offset(4.dp.toPx(), centerY - targetWindowHeight / 2f),
                    end = Offset(size.width - 4.dp.toPx(), centerY - targetWindowHeight / 2f),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = InstrumentGreen,
                    start = Offset(4.dp.toPx(), centerY + targetWindowHeight / 2f),
                    end = Offset(size.width - 4.dp.toPx(), centerY + targetWindowHeight / 2f),
                    strokeWidth = 2.dp.toPx()
                )

                // Bubble movement along Y
                val maxTravel = (vialHeight / 2f) - 18.dp.toPx()
                val bubbleOffset = (-animatedPitch / 30f).coerceIn(-1f, 1f) * maxTravel
                val bubbleCenter = Offset(centerX, centerY + bubbleOffset)
                val bubbleColor = if (isLevel) InstrumentGreen else InstrumentYellow

                drawCircle(
                    color = bubbleColor.copy(alpha = 0.4f),
                    radius = 16.dp.toPx(),
                    center = bubbleCenter
                )
                drawCircle(
                    color = bubbleColor,
                    radius = 11.dp.toPx(),
                    center = bubbleCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(bubbleCenter.x - 2.dp.toPx(), bubbleCenter.y - 2.dp.toPx())
                )
            }
        }
    }
}
