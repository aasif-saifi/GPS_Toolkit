package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedLocation
import com.example.model.AppSettings
import com.example.model.LocationData
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.UnitConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CoordinatesScreen(
    locationData: LocationData,
    settings: AppSettings,
    savedLocations: List<SavedLocation>,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSaveLocation: (String, String) -> Unit,
    onDeleteLocation: (SavedLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }
    var waypointName by remember { mutableStateOf("") }
    var waypointNote by remember { mutableStateOf("") }

    val latStr = String.format(Locale.US, "%.6f", locationData.latitude)
    val lngStr = String.format(Locale.US, "%.6f", locationData.longitude)
    val latDms = UnitConverter.toDms(locationData.latitude, true)
    val lngDms = UnitConverter.toDms(locationData.longitude, false)
    val ddString = "$latStr, $lngStr"
    val dmsString = "$latDms, $lngDms"

    val timeFormatted = if (locationData.timestampMs > 0) {
        val sdf = SimpleDateFormat("HH:mm:ss dd-MMM-yyyy", Locale.getDefault())
        sdf.format(Date(locationData.timestampMs))
    } else "Waiting for GPS fix..."

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Waypoint", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Current coordinates: $ddString",
                        color = InstrumentCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = waypointName,
                        onValueChange = { waypointName = it },
                        label = { Text("Waypoint Name") },
                        placeholder = { Text("e.g. Basecamp, Summit, Car") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InstrumentCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("waypoint_name_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = waypointNote,
                        onValueChange = { waypointNote = it },
                        label = { Text("Note (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InstrumentCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("waypoint_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveLocation(waypointName, waypointNote)
                        waypointName = ""
                        waypointNote = ""
                        showSaveDialog = false
                        Toast.makeText(context, "Waypoint saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = InstrumentCyan),
                    modifier = Modifier.testTag("confirm_save_waypoint_button")
                ) {
                    Text("Save", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("coordinates_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                Button(
                    onClick = { showSaveDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = InstrumentCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp).testTag("save_waypoint_button")
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAVE WAYPOINT", color = Color(0xFF00363D), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        if (!hasLocationPermission) {
            item {
                PermissionRequestBanner(onRequestPermission = onRequestPermission)
            }
        }

        // Decimal Degrees Card with Copy & Action Buttons
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DECIMAL DEGREES (DD)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = {
                                copyToClipboard(context, ddString, "Decimal Coordinates")
                            },
                            modifier = Modifier.size(32.dp).testTag("copy_dd_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy DD", tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("LATITUDE", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(latStr, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("LONGITUDE", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Text(lngStr, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // DMS Format Section
                    Text(
                        text = "DEGREES, MINUTES, SECONDS (DMS)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dmsString,
                            color = InstrumentCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = {
                                copyToClipboard(context, dmsString, "DMS Coordinates")
                            },
                            modifier = Modifier.size(32.dp).testTag("copy_dms_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy DMS", tint = InstrumentCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Share & Open in Maps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { shareLocation(context, locationData, ddString, dmsString) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("share_location_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SHARE", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { openInMaps(context, locationData.latitude, locationData.longitude) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("open_maps_button")
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("OPEN MAPS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Reverse Geocoded Address Card
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
                        text = "ESTIMATED ADDRESS / LOCATION",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = locationData.address ?: "Resolving street address from coordinates...",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "LAST UPDATED: $timeFormatted",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Saved Waypoints Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SAVED WAYPOINTS (${savedLocations.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (savedLocations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No saved waypoints yet", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Tap 'SAVE WAYPOINT' above to save points of interest locally.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(savedLocations, key = { it.id }) { waypoint ->
                SavedWaypointItem(
                    waypoint = waypoint,
                    onOpenMaps = { openInMaps(context, waypoint.latitude, waypoint.longitude) },
                    onCopy = {
                        val str = "${waypoint.name}: ${waypoint.latitude}, ${waypoint.longitude}"
                        copyToClipboard(context, str, "Waypoint Coordinates")
                    },
                    onDelete = { onDeleteLocation(waypoint) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SavedWaypointItem(
    waypoint: SavedLocation,
    onOpenMaps: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(InstrumentCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = InstrumentCyan, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = waypoint.name,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format(Locale.US, "%.5f, %.5f", waypoint.latitude, waypoint.longitude),
                    color = InstrumentCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                if (waypoint.note.isNotBlank()) {
                    Text(
                        text = waypoint.note,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            IconButton(onClick = onOpenMaps, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Open in Maps", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Coordinates", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Waypoint", tint = InstrumentOrange, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareLocation(context: Context, loc: LocationData, dd: String, dms: String) {
    val shareBody = buildString {
        append("GPS Location:\n")
        append("Decimal: $dd\n")
        append("DMS: $dms\n")
        append("Altitude: ${loc.altitudeMeters.toInt()} m\n")
        if (!loc.address.isNullOrBlank()) {
            append("Address: ${loc.address}\n")
        }
        append("https://maps.google.com/?q=${loc.latitude},${loc.longitude}")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "My GPS Coordinates")
        putExtra(Intent.EXTRA_TEXT, shareBody)
    }
    context.startActivity(Intent.createChooser(intent, "Share Location"))
}

private fun openInMaps(context: Context, latitude: Double, longitude: Double) {
    try {
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Waypoint)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No map application installed", Toast.LENGTH_SHORT).show()
    }
}
