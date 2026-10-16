package com.example.ui.screens

import android.hardware.SensorManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.CalibrationHelpDialog
import com.example.ui.components.GpsStatusPill
import com.example.ui.components.TacticalCompassDial
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
import com.example.ui.theme.TextTertiary
import com.example.util.UnitConverter
import java.util.Locale

@Composable
fun CompassScreen(
    compassData: CompassData,
    levelData: LevelData,
    locationData: LocationData,
    settings: AppSettings,
    onToggleNorthReference: (NorthReference) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCalibrationHelp by remember { mutableStateOf(false) }

    if (showCalibrationHelp) {
        CalibrationHelpDialog(onDismiss = { showCalibrationHelp = false })
    }

    val isTrueNorth = settings.northReference == NorthReference.TRUE && compassData.isTrueNorthAvailable
    val currentHeading = if (isTrueNorth) compassData.trueAzimuth else compassData.magneticAzimuth
    val cardinal = UnitConverter.headingToCardinal(currentHeading)

    val accuracyLabel = when (compassData.accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH"
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM"
        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW"
        else -> "UNRELIABLE"
    }

    val accuracyColor = when (compassData.accuracy) {
        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> InstrumentGreen
        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> InstrumentYellow
        else -> InstrumentOrange
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("compass_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Top Controls: True vs Magnetic North Switch, Calibration Help
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // North Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    val isMagneticSelected = settings.northReference == NorthReference.MAGNETIC
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isMagneticSelected) InstrumentCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isMagneticSelected) InstrumentCyan else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onToggleNorthReference(NorthReference.MAGNETIC) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MAGNETIC N",
                            color = if (isMagneticSelected) InstrumentCyan else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    val isTrueSelected = settings.northReference == NorthReference.TRUE
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTrueSelected) InstrumentOrange.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isTrueSelected) InstrumentOrange else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onToggleNorthReference(NorthReference.TRUE) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TRUE N",
                            color = if (isTrueSelected) InstrumentOrange else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Sensor Accuracy & Calibration trigger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .clickable { showCalibrationHelp = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(6.dp).clip(CircleShape).background(accuracyColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CALIB: $accuracyLabel",
                        color = accuracyColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Calibrate",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Uncalibrated / Warning notification if accuracy is low
        if (compassData.accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalibrationHelp = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2211)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = InstrumentYellow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Compass interference detected. Wave device in figure-8 motion to calibrate.",
                            color = InstrumentYellow,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Big Digital Heading Display (e.g. 323° ▼ NW) matching compass.jpg
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.0f°", currentHeading),
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "▼",
                        color = InstrumentOrange,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cardinal,
                        color = InstrumentCyan,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (isTrueNorth) {
                    Text(
                        text = String.format(Locale.US, "DECLINATION: %+.1f°", compassData.declinationDegrees),
                        color = InstrumentOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Center Rotating Compass Dial with Bullseye Level
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(310.dp)) {
                        TacticalCompassDial(
                            azimuthDegrees = currentHeading,
                            pitchDegrees = levelData.pitchDegrees,
                            rollDegrees = levelData.rollDegrees,
                            showCenterBubble = true
                        )
                    }
                }
            }
        }

        // Course GPS & Magnetic Field Strength Row (as in compass.jpg)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TacticalMetricCard(
                    title = "COURSE GPS",
                    value = if (locationData.isGpsFixed && locationData.bearingDegrees > 0f) String.format(Locale.US, "%.0f°", locationData.bearingDegrees) else "--",
                    icon = Icons.Default.Navigation,
                    valueColor = InstrumentCyan,
                    modifier = Modifier.weight(1f)
                )
                TacticalMetricCard(
                    title = "MAGN. FIELD",
                    value = String.format(Locale.US, "%.0f", compassData.magneticFieldMicroTesla),
                    unit = "µT",
                    icon = Icons.Default.CompassCalibration,
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Coordinates Bar in DMS (as in compass.jpg)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COORDINATES (DMS)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${UnitConverter.toDms(locationData.latitude, true)}  ${UnitConverter.toDms(locationData.longitude, false)}",
                        color = InstrumentCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
