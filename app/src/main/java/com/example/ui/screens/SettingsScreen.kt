package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.AltitudeUnit
import com.example.model.AppThemeMode
import com.example.model.AppSettings
import com.example.model.CompassData
import com.example.model.LevelData
import com.example.model.LocationData
import com.example.model.NorthReference
import com.example.model.PressureData
import com.example.model.SpeedUnit
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.InstrumentGreen
import com.example.ui.theme.InstrumentOrange
import com.example.ui.theme.InstrumentYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: AppSettings,
    locationData: LocationData,
    compassData: CompassData,
    levelData: LevelData,
    pressureData: PressureData,
    onUpdateSpeedUnit: (SpeedUnit) -> Unit,
    onUpdateAltitudeUnit: (AltitudeUnit) -> Unit,
    onUpdateNorthReference: (NorthReference) -> Unit,
    onUpdateMaxSpeed: (Float) -> Unit,
    onUpdateSpeedLimitWarning: (Float) -> Unit,
    onUpdateLevelTolerance: (Float) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleKeepScreenOn: (Boolean) -> Unit,
    onUpdateTheme: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "APP SETTINGS & PREFERENCES",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "MEASUREMENT UNITS & HARDWARE CONTROLS",
                color = InstrumentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Measurement Units Group
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MEASUREMENT UNITS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speed Unit
                    Text("Speed Unit", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(SpeedUnit.KMH, SpeedUnit.MPH, SpeedUnit.KNOTS).forEach { unit ->
                            val isSelected = settings.speedUnit == unit
                            UnitOptionChip(
                                label = unit.shortLabel,
                                isSelected = isSelected,
                                onClick = { onUpdateSpeedUnit(unit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Altitude Unit
                    Text("Altitude Unit", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(AltitudeUnit.METERS, AltitudeUnit.FEET).forEach { unit ->
                            val isSelected = settings.altitudeUnit == unit
                            UnitOptionChip(
                                label = unit.label,
                                isSelected = isSelected,
                                onClick = { onUpdateAltitudeUnit(unit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // North Reference
                    Text("North Bearing Reference", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(NorthReference.MAGNETIC, NorthReference.TRUE).forEach { ref ->
                            val isSelected = settings.northReference == ref
                            UnitOptionChip(
                                label = ref.label,
                                isSelected = isSelected,
                                onClick = { onUpdateNorthReference(ref) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Instrument Gauge Calibration Group
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INSTRUMENT SCALES & TOLERANCES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Max Gauge Scale
                    Text("Analog Gauge Top Speed", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(120f, 160f, 200f, 240f).forEach { maxSpd ->
                            val isSelected = settings.maxGaugeSpeed == maxSpd
                            UnitOptionChip(
                                label = "${maxSpd.toInt()}",
                                isSelected = isSelected,
                                onClick = { onUpdateMaxSpeed(maxSpd) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speed Limit Alarm
                    Text("Speed Limit Alarm Alert", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0f, 60f, 80f, 100f, 120f).forEach { limit ->
                            val isSelected = settings.speedLimitWarning == limit
                            UnitOptionChip(
                                label = if (limit == 0f) "Off" else "${limit.toInt()}",
                                isSelected = isSelected,
                                onClick = { onUpdateSpeedLimitWarning(limit) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Level Tolerance
                    Text("Spirit Level Tolerance Window", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { tol ->
                            val isSelected = settings.levelTolerance == tol
                            UnitOptionChip(
                                label = "±${tol}°",
                                isSelected = isSelected,
                                onClick = { onUpdateLevelTolerance(tol) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Toggles: Haptics & Keep Screen On
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Haptic Feedback", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Vibrate when surface is level and when exceeding speed limit", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = onToggleHaptic,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = InstrumentCyan,
                                checkedTrackColor = InstrumentCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("haptic_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Keep Screen On", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Prevent device from sleeping during telemetry and navigation", color = TextSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.keepScreenOn,
                            onCheckedChange = onToggleKeepScreenOn,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = InstrumentCyan,
                                checkedTrackColor = InstrumentCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("keep_screen_on_switch")
                        )
                    }
                }
            }
        }

        // Hardware Diagnostics Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DEVICE SENSOR DIAGNOSTICS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DiagnosticItem("GPS Location Provider", if (locationData.isGpsFixed) "LOCKED" else "SEARCHING", if (locationData.isGpsFixed) InstrumentGreen else InstrumentOrange)
                    DiagnosticItem("Rotation Vector / Gyroscope", if (compassData.hasCompassSensor) "DETECTED" else "UNAVAILABLE", if (compassData.hasCompassSensor) InstrumentGreen else InstrumentOrange)
                    DiagnosticItem("3-Axis Accelerometer", if (levelData.hasAccelerometer) "DETECTED" else "UNAVAILABLE", if (levelData.hasAccelerometer) InstrumentGreen else InstrumentOrange)
                    DiagnosticItem("Barometric Pressure Sensor", if (pressureData.isAvailable) "DETECTED" else "NOT AVAILABLE", if (pressureData.isAvailable) InstrumentGreen else TextSecondary)
                    DiagnosticItem("True North Geomagnetic Model", if (compassData.isTrueNorthAvailable) "CALCULATED" else "AWAITING GPS", if (compassData.isTrueNorthAvailable) InstrumentGreen else InstrumentYellow)
                }
            }
        }

        // Privacy & Offline Operation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = InstrumentGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("100% Offline & Private Telemetry", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All GPS coordinates, compass headings, speeds, and saved waypoints are calculated locally on your device. Zero telemetry data is sent to external servers.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DiagnosticItem(name: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = TextPrimary, fontSize = 12.sp)
        Text(
            text = status,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun UnitOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) InstrumentCyan else DarkSurfaceVariant)
            .border(1.dp, if (isSelected) InstrumentCyan else DarkBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color(0xFF00363D) else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
