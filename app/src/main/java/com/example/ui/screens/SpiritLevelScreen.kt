package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.LevelData
import com.example.ui.components.CircularBullseyeLevel
import com.example.ui.components.TacticalMetricCard
import com.example.ui.components.TubularHorizontalVial
import com.example.ui.components.TubularVerticalVial
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.InstrumentGreen
import com.example.ui.theme.InstrumentYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SpiritLevelScreen(
    levelData: LevelData,
    settings: AppSettings,
    onCalibrateZero: () -> Unit,
    onResetCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("spirit_level_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Status Header: Level condition & tolerance indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level status badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (levelData.isLevel) Color(0xFF0F2B1D) else Color(0xFF262214))
                        .border(1.dp, if (levelData.isLevel) InstrumentGreen else InstrumentYellow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (levelData.isLevel) Icons.Default.CheckCircle else Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (levelData.isLevel) InstrumentGreen else InstrumentYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (levelData.isLevel) "SURFACE LEVEL" else "TILTED",
                        color = if (levelData.isLevel) InstrumentGreen else InstrumentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Tolerance indicator
                Text(
                    text = String.format(Locale.US, "TOLERANCE: ±%.1f°", settings.levelTolerance),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2D Circular Bullseye Level with side tubular vertical vial
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "2D BULLSEYE SURFACE LEVEL",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Circular Bullseye Center
                        Box(modifier = Modifier.size(240.dp)) {
                            CircularBullseyeLevel(
                                pitchDegrees = levelData.pitchDegrees,
                                rollDegrees = levelData.rollDegrees,
                                toleranceDegrees = settings.levelTolerance
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Vertical Vial on the side
                        TubularVerticalVial(
                            pitchDegrees = levelData.pitchDegrees,
                            toleranceDegrees = settings.levelTolerance
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Horizontal Tubular Vial
                    TubularHorizontalVial(
                        rollDegrees = levelData.rollDegrees,
                        toleranceDegrees = settings.levelTolerance,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Digital Angle Readout Cards (Pitch & Roll)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isPitchLevel = kotlin.math.abs(levelData.pitchDegrees) <= settings.levelTolerance
                val isRollLevel = kotlin.math.abs(levelData.rollDegrees) <= settings.levelTolerance

                TacticalMetricCard(
                    title = "Pitch (Y-Axis)",
                    value = String.format(Locale.US, "%+.1f°", levelData.pitchDegrees),
                    valueColor = if (isPitchLevel) InstrumentGreen else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                TacticalMetricCard(
                    title = "Roll (X-Axis)",
                    value = String.format(Locale.US, "%+.1f°", levelData.rollDegrees),
                    valueColor = if (isRollLevel) InstrumentGreen else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calibration Controls (Calibrate Zero & Reset Offset)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SURFACE CALIBRATION",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onCalibrateZero,
                            colors = ButtonDefaults.buttonColors(containerColor = InstrumentCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("calibrate_zero_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CALIBRATE ZERO", color = Color(0xFF00363D), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        if (levelData.isCalibrated) {
                            OutlinedButton(
                                onClick = onResetCalibration,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(44.dp).testTag("reset_calibration_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    if (levelData.isCalibrated) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "Custom offset applied: P: %+.1f°, R: %+.1f°", levelData.calibratedPitchOffset, levelData.calibratedRollOffset),
                            color = InstrumentCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Feedback Tip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Haptic pulse vibrates automatically when device is within ±${settings.levelTolerance}° of perfect level. Camera bumps on the phone back may cause a slight tilt when laid flat without a case.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
