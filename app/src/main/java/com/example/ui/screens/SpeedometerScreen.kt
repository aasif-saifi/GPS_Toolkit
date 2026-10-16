package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.LocationData
import com.example.model.SpeedUnit
import com.example.model.SpeedometerSession
import com.example.ui.components.AnalogSpeedometerGauge
import com.example.ui.components.GpsStatusPill
import com.example.ui.components.PermissionRequestBanner
import com.example.ui.components.TacticalMetricCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.InstrumentGreen
import com.example.ui.theme.InstrumentOrange
import com.example.ui.theme.InstrumentRed
import com.example.ui.theme.InstrumentYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.UnitConverter
import java.util.Locale

@Composable
fun SpeedometerScreen(
    locationData: LocationData,
    session: SpeedometerSession,
    settings: AppSettings,
    isSpeedLimitExceeded: Boolean,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartTrip: () -> Unit,
    onPauseTrip: () -> Unit,
    onResetTrip: () -> Unit,
    onToggleUnit: (SpeedUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDisplayMode by remember { mutableIntStateOf(0) } // 0 = Analog, 1 = Large Digital HUD
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val currentSpeed = UnitConverter.speedFromMps(locationData.speedMetersPerSec, settings.speedUnit)
    val maxSpeed = UnitConverter.speedFromMps(session.maxSpeedMps, settings.speedUnit)
    val avgSpeed = UnitConverter.speedFromMps(session.averageSpeedMps, settings.speedUnit)
    val distanceStr = UnitConverter.formatDistance(session.totalDistanceMeters, settings.speedUnit)
    val elapsedTimeStr = UnitConverter.formatElapsedTime(session.elapsedTimeMs)

    if (isLandscape) {
        // Landscape Split Screen
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp)
                .testTag("speedometer_screen_landscape"),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Half: Gauge or Digital Readout
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedDisplayMode == 0) {
                    AnalogSpeedometerGauge(
                        currentSpeed = currentSpeed,
                        maxSpeed = settings.maxGaugeSpeed,
                        unitLabel = settings.speedUnit.shortLabel,
                        isWarning = isSpeedLimitExceeded
                    )
                } else {
                    LargeDigitalSpeedDisplay(
                        currentSpeed = currentSpeed,
                        unitLabel = settings.speedUnit.shortLabel,
                        isWarning = isSpeedLimitExceeded
                    )
                }
            }

            // Right Half: Trip Statistics, Controls, and Unit Toggle
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mode Toggle & GPS status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DisplayModeToggle(
                        selectedMode = selectedDisplayMode,
                        onSelectMode = { selectedDisplayMode = it }
                    )
                    UnitSelectorPill(
                        currentUnit = settings.speedUnit,
                        onSelectUnit = onToggleUnit
                    )
                }

                // Stats Cards
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TacticalMetricCard("Max Speed", String.format(Locale.US, "%.0f", maxSpeed), settings.speedUnit.shortLabel, modifier = Modifier.weight(1f))
                        TacticalMetricCard("Avg Speed", String.format(Locale.US, "%.0f", avgSpeed), settings.speedUnit.shortLabel, modifier = Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TacticalMetricCard("Trip Distance", distanceStr, modifier = Modifier.weight(1f))
                        TacticalMetricCard("Trip Time", elapsedTimeStr, modifier = Modifier.weight(1f))
                    }
                }

                // Trip Controls
                TripControlButtons(
                    isRunning = session.isRunning,
                    isPaused = session.isPaused,
                    onStart = onStartTrip,
                    onPause = onPauseTrip,
                    onReset = onResetTrip
                )
            }
        }
    } else {
        // Portrait Layout
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(horizontal = 16.dp)
                .testTag("speedometer_screen_portrait"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Top Action Bar: GPS Status, Unit Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GpsStatusPill(
                        isGpsFixed = locationData.isGpsFixed,
                        accuracyMeters = locationData.horizontalAccuracyMeters,
                        provider = locationData.provider
                    )
                    UnitSelectorPill(
                        currentUnit = settings.speedUnit,
                        onSelectUnit = onToggleUnit
                    )
                }
            }

            if (!hasLocationPermission) {
                item {
                    PermissionRequestBanner(onRequestPermission = onRequestPermission)
                }
            }

            // Speed Limit Warning Banner
            if (isSpeedLimitExceeded) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = InstrumentRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SPEED LIMIT EXCEEDED (> ${settings.speedLimitWarning.toInt()} ${settings.speedUnit.shortLabel})",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Display Mode Switcher (Analog Gauge vs Digital HUD)
            item {
                DisplayModeToggle(
                    selectedMode = selectedDisplayMode,
                    onSelectMode = { selectedDisplayMode = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Main Speedometer Area
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedDisplayMode == 0) {
                            Box(modifier = Modifier.size(290.dp)) {
                                AnalogSpeedometerGauge(
                                    currentSpeed = currentSpeed,
                                    maxSpeed = settings.maxGaugeSpeed,
                                    unitLabel = settings.speedUnit.shortLabel,
                                    isWarning = isSpeedLimitExceeded
                                )
                            }
                        } else {
                            LargeDigitalSpeedDisplay(
                                currentSpeed = currentSpeed,
                                unitLabel = settings.speedUnit.shortLabel,
                                isWarning = isSpeedLimitExceeded,
                                modifier = Modifier.height(290.dp)
                            )
                        }
                    }
                }
            }

            // Trip Session Controls (Start, Pause, Reset)
            item {
                TripControlButtons(
                    isRunning = session.isRunning,
                    isPaused = session.isPaused,
                    onStart = onStartTrip,
                    onPause = onPauseTrip,
                    onReset = onResetTrip,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2x2 Session Statistics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TacticalMetricCard(
                        title = "Max Speed",
                        value = String.format(Locale.US, "%.0f", maxSpeed),
                        unit = settings.speedUnit.shortLabel,
                        valueColor = InstrumentOrange,
                        modifier = Modifier.weight(1f)
                    )
                    TacticalMetricCard(
                        title = "Avg Speed",
                        value = String.format(Locale.US, "%.0f", avgSpeed),
                        unit = settings.speedUnit.shortLabel,
                        valueColor = InstrumentCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TacticalMetricCard(
                        title = "Trip Distance",
                        value = distanceStr,
                        valueColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    TacticalMetricCard(
                        title = "Elapsed Time",
                        value = elapsedTimeStr,
                        valueColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LargeDigitalSpeedDisplay(
    currentSpeed: Float,
    unitLabel: String,
    isWarning: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DIGITAL HUD",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format(Locale.US, "%.0f", currentSpeed),
            color = if (isWarning) InstrumentRed else InstrumentCyan,
            fontSize = 96.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-2).sp
        )
        Text(
            text = unitLabel.uppercase(),
            color = if (isWarning) InstrumentRed else TextSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DisplayModeToggle(
    selectedMode: Int,
    onSelectMode: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        val modes = listOf("ANALOG GAUGE", "DIGITAL HUD")
        modes.forEachIndexed { index, title ->
            val isSelected = selectedMode == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) InstrumentCyan else Color.Transparent)
                    .clickable { onSelectMode(index) }
                    .padding(vertical = 8.dp)
                    .testTag("mode_toggle_$index"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color(0xFF00363D) else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun UnitSelectorPill(
    currentUnit: SpeedUnit,
    onSelectUnit: (SpeedUnit) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(2.dp)
    ) {
        val units = listOf(SpeedUnit.KMH, SpeedUnit.MPH)
        units.forEach { unit ->
            val isSelected = currentUnit == unit
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) InstrumentCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) InstrumentCyan else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectUnit(unit) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("unit_pill_${unit.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.shortLabel,
                    color = if (isSelected) InstrumentCyan else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun TripControlButtons(
    isRunning: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isRunning || isPaused) {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = InstrumentGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(46.dp).testTag("start_trip_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00391A))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPaused) "RESUME TRIP" else "START TRIP",
                    color = Color(0xFF00391A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Button(
                onClick = onPause,
                colors = ButtonDefaults.buttonColors(containerColor = InstrumentYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(46.dp).testTag("pause_trip_button")
            ) {
                Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFF3E2D00))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PAUSE TRIP",
                    color = Color(0xFF3E2D00),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        OutlinedButton(
            onClick = onReset,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(46.dp).testTag("reset_trip_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RESET", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
    }
}
