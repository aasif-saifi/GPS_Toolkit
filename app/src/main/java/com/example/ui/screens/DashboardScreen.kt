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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.CompassData
import com.example.model.LevelData
import com.example.model.LocationData
import com.example.model.NorthReference
import com.example.model.SpeedUnit
import com.example.ui.components.AnalogSpeedometerGauge
import com.example.ui.components.CalibrationHelpDialog
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
fun DashboardScreen(
    locationData: LocationData,
    compassData: CompassData,
    levelData: LevelData,
    settings: AppSettings,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateToTool: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showHelpDialog) {
        CalibrationHelpDialog(onDismiss = { showHelpDialog = false })
    }

    val currentSpeed = UnitConverter.speedFromMps(locationData.speedMetersPerSec, settings.speedUnit)
    val currentAlt = UnitConverter.altitudeFromMeters(locationData.altitudeMeters, settings.altitudeUnit)
    val heading = if (settings.northReference == NorthReference.TRUE && compassData.isTrueNorthAvailable) {
        compassData.trueAzimuth
    } else {
        compassData.magneticAzimuth
    }
    val cardinal = UnitConverter.headingToCardinal(heading)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header Bar: Title, GPS Status Pill, Help button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GPS TOOLKIT",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TACTICAL COCKPIT",
                        color = InstrumentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GpsStatusPill(
                        isGpsFixed = locationData.isGpsFixed,
                        accuracyMeters = locationData.horizontalAccuracyMeters,
                        provider = locationData.provider
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier.size(36.dp).testTag("dashboard_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "Calibration Guide",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Permission Banner if location not granted
        if (!hasLocationPermission) {
            item {
                PermissionRequestBanner(onRequestPermission = onRequestPermission)
            }
        }

        // Hero Gauge Card: Analog Speedometer + Mini Live Indicators
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .clickable { onNavigateToTool(1) }
                    .testTag("hero_speedometer_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPEEDOMETER",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VIEW FULL",
                                color = InstrumentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = InstrumentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Analog Gauge centered
                    Box(modifier = Modifier.size(240.dp)) {
                        AnalogSpeedometerGauge(
                            currentSpeed = currentSpeed,
                            maxSpeed = settings.maxGaugeSpeed,
                            unitLabel = settings.speedUnit.shortLabel
                        )
                    }

                    // Location summary line below gauge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.5f, %.5f", locationData.latitude, locationData.longitude),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (locationData.isGpsFixed) "GPS LOCK" else "ACQUIRING...",
                            color = if (locationData.isGpsFixed) InstrumentGreen else InstrumentOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Tactical 2x2 Metric Grid (Altitude, Heading, Level, Accuracy)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalMetricCard(
                    title = "Altitude",
                    value = String.format(Locale.US, "%.0f", currentAlt),
                    unit = settings.altitudeUnit.shortLabel,
                    icon = Icons.Default.Height,
                    valueColor = InstrumentCyan,
                    modifier = Modifier.weight(1f).clickable { onNavigateToTool(2) }
                )
                TacticalMetricCard(
                    title = "Heading",
                    value = String.format(Locale.US, "%.0f° %s", heading, cardinal),
                    icon = Icons.Default.Navigation,
                    valueColor = InstrumentOrange,
                    modifier = Modifier.weight(1f).clickable { onNavigateToTool(3) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalMetricCard(
                    title = "Spirit Level",
                    value = if (levelData.isLevel) "LEVEL 0°" else String.format(Locale.US, "P:%.0f° R:%.0f°", levelData.pitchDegrees, levelData.rollDegrees),
                    icon = Icons.Default.Explore,
                    valueColor = if (levelData.isLevel) InstrumentGreen else InstrumentYellow,
                    modifier = Modifier.weight(1f).clickable { onNavigateToTool(4) }
                )
                TacticalMetricCard(
                    title = "Accuracy",
                    value = if (locationData.isGpsFixed) String.format(Locale.US, "±%.0f", locationData.horizontalAccuracyMeters) else "--",
                    unit = "m",
                    icon = Icons.Default.LocationOn,
                    valueColor = if (locationData.horizontalAccuracyMeters <= 5f) InstrumentGreen else InstrumentYellow,
                    modifier = Modifier.weight(1f).clickable { onNavigateToTool(5) }
                )
            }
        }

        // Quick-Access Toolkit Launchers
        item {
            Text(
                text = "TOOLKIT MODULES",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ToolkitToolItem(
                    title = "Speedometer",
                    subtitle = "Analog gauge & large digital HUD with trip tracker",
                    icon = Icons.Default.Speed,
                    accentColor = InstrumentCyan,
                    onClick = { onNavigateToTool(1) }
                )
                ToolkitToolItem(
                    title = "Altimeter & Elevation",
                    subtitle = "GPS & barometric MSL altitude, relative climb/descent",
                    icon = Icons.Default.Height,
                    accentColor = InstrumentCyan,
                    onClick = { onNavigateToTool(2) }
                )
                ToolkitToolItem(
                    title = "Tactical Compass",
                    subtitle = "Magnetic & true north rotating dial with bullseye level",
                    icon = Icons.Default.Navigation,
                    accentColor = InstrumentOrange,
                    onClick = { onNavigateToTool(3) }
                )
                ToolkitToolItem(
                    title = "Spirit / Bubble Level",
                    subtitle = "2D Bullseye & tubular vials with angle readouts & zeroing",
                    icon = Icons.Default.Explore,
                    accentColor = InstrumentGreen,
                    onClick = { onNavigateToTool(4) }
                )
                ToolkitToolItem(
                    title = "GPS Coordinates & Waypoints",
                    subtitle = "DD, DMS, reverse address, sharing & saved waypoints",
                    icon = Icons.Default.LocationOn,
                    accentColor = InstrumentCyan,
                    onClick = { onNavigateToTool(5) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolkitToolItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
