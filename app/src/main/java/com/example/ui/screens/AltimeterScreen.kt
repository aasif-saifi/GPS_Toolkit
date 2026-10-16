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
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
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
import com.example.model.AltimeterSession
import com.example.model.AltitudeUnit
import com.example.model.AppSettings
import com.example.model.LocationData
import com.example.model.PressureData
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
import com.example.ui.theme.InstrumentYellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.UnitConverter
import java.util.Locale

@Composable
fun AltimeterScreen(
    locationData: LocationData,
    pressureData: PressureData,
    altimeterSession: AltimeterSession,
    settings: AppSettings,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onResetBaseline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentGpsAlt = UnitConverter.altitudeFromMeters(locationData.altitudeMeters, settings.altitudeUnit)
    val baselineAlt = altimeterSession.baselineAltitudeMeters?.let {
        UnitConverter.altitudeFromMeters(it, settings.altitudeUnit)
    } ?: currentGpsAlt

    val relativeChange = currentGpsAlt - baselineAlt

    val minAlt = if (altimeterSession.minAltitudeMeters != Double.MAX_VALUE) {
        UnitConverter.altitudeFromMeters(altimeterSession.minAltitudeMeters, settings.altitudeUnit)
    } else currentGpsAlt

    val maxAlt = if (altimeterSession.maxAltitudeMeters != Double.MIN_VALUE) {
        UnitConverter.altitudeFromMeters(altimeterSession.maxAltitudeMeters, settings.altitudeUnit)
    } else currentGpsAlt

    val baroAlt = UnitConverter.altitudeFromMeters(pressureData.barometricAltitudeMeters.toDouble(), settings.altitudeUnit)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("altimeter_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
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
                Text(
                    text = "UNIT: ${settings.altitudeUnit.shortLabel.uppercase()}",
                    color = InstrumentCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (!hasLocationPermission) {
            item {
                PermissionRequestBanner(onRequestPermission = onRequestPermission)
            }
        }

        // Hero Elevation Display
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ALTITUDE ABOVE MEAN SEA LEVEL (MSL)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.0f", currentGpsAlt),
                            color = InstrumentCyan,
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = settings.altitudeUnit.shortLabel,
                            color = InstrumentCyan,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Accuracy & Source
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val vertAcc = locationData.verticalAccuracyMeters?.let { "±${it.toInt()}m" } ?: "±8m est."
                        Text(
                            text = "GPS VERTICAL ACCURACY: $vertAcc",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Relative Altitude Card (Gain / Loss session)
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
                        Text(
                            text = "RELATIVE CLIMB / DESCENT",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        OutlinedButton(
                            onClick = onResetBaseline,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp).testTag("reset_baseline_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SET ZERO", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val sign = if (relativeChange >= 0) "+" else ""
                    val changeColor = if (relativeChange >= 0) InstrumentGreen else InstrumentOrange

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$sign${String.format(Locale.US, "%.1f", relativeChange)}",
                            color = changeColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = settings.altitudeUnit.shortLabel,
                            color = changeColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Text(
                        text = "Relative to baseline: ${String.format(Locale.US, "%.0f", baselineAlt)} ${settings.altitudeUnit.shortLabel}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Min & Max Altitude Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalMetricCard(
                    title = "Minimum Alt",
                    value = String.format(Locale.US, "%.0f", minAlt),
                    unit = settings.altitudeUnit.shortLabel,
                    icon = Icons.Default.VerticalAlignBottom,
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                TacticalMetricCard(
                    title = "Maximum Alt",
                    value = String.format(Locale.US, "%.0f", maxAlt),
                    unit = settings.altitudeUnit.shortLabel,
                    icon = Icons.Default.VerticalAlignTop,
                    valueColor = InstrumentCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Barometric Pressure Sensor Card
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
                        Text(
                            text = "BAROMETRIC SENSOR",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (pressureData.isAvailable) "HARDWARE ACTIVE" else "NOT DETECTED",
                            color = if (pressureData.isAvailable) InstrumentGreen else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (pressureData.isAvailable) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("ATMOSPHERIC PRESSURE", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = String.format(Locale.US, "%.1f hPa", pressureData.pressureHpa),
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PRESSURE ALTITUDE", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = String.format(Locale.US, "%.0f %s", baroAlt, settings.altitudeUnit.shortLabel),
                                    color = InstrumentCyan,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Device lacks built-in barometric pressure sensor. Altimeter is operating in GPS Geoid Elevation mode.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Educational Disclaimer Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GPS vertical accuracy is typically 2x to 3x lower than horizontal accuracy due to satellite orbital geometry. Barometric altitude provides superior relative precision for stairs and hill climbs when calibrated.",
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
