package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AppThemeMode
import com.example.ui.screens.AltimeterScreen
import com.example.ui.screens.CompassScreen
import com.example.ui.screens.CoordinatesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpeedometerScreen
import com.example.ui.screens.SpiritLevelScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.InstrumentCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GpsToolkitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GpsToolkitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            // Keep screen on management
            DisposableEffect(settings.keepScreenOn) {
                if (settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            val darkTheme = when (settings.themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                GpsToolkitApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }
}

@Composable
fun GpsToolkitApp(viewModel: GpsToolkitViewModel) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val locationData by viewModel.locationData.collectAsState()
    val compassData by viewModel.compassData.collectAsState()
    val levelData by viewModel.levelData.collectAsState()
    val pressureData by viewModel.pressureData.collectAsState()
    val session by viewModel.speedometerSession.collectAsState()
    val altimeterSession by viewModel.altimeterSession.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val isSpeedLimitExceeded by viewModel.isSpeedLimitExceeded.collectAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()

    // Permission launcher for Location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.setLocationPermissionGranted(fineGranted || coarseGranted)
    }

    // Check permissions on first composition
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            viewModel.setLocationPermissionGranted(true)
        } else {
            // Automatically prompt on first launch
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // BackHandler: Return to Dashboard if on another tab
    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    val navItems = listOf(
        NavigationItem("Dash", Icons.Default.Dashboard, "Dashboard"),
        NavigationItem("Speed", Icons.Default.Speed, "Speedometer"),
        NavigationItem("Altitude", Icons.Default.Height, "Altimeter"),
        NavigationItem("Compass", Icons.Default.Navigation, "Compass"),
        NavigationItem("Level", Icons.Default.Explore, "Spirit Level"),
        NavigationItem("GPS", Icons.Default.LocationOn, "Coordinates"),
        NavigationItem("Settings", Icons.Default.Settings, "Settings")
    )

    if (isWideScreen) {
        // Wide screen / Tablet layout with NavigationRail
        Row(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
            NavigationRail(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                modifier = Modifier.fillMaxHeight().border(1.dp, DarkBorder)
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationRailItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.shortTitle, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = InstrumentCyan,
                            selectedTextColor = InstrumentCyan,
                            indicatorColor = InstrumentCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_rail_${item.shortTitle.lowercase()}")
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ScreenContent(
                    selectedTab = selectedTab,
                    viewModel = viewModel,
                    locationData = locationData,
                    compassData = compassData,
                    levelData = levelData,
                    pressureData = pressureData,
                    session = session,
                    altimeterSession = altimeterSession,
                    settings = settings,
                    savedLocations = savedLocations,
                    isSpeedLimitExceeded = isSpeedLimitExceeded,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onNavigateToTool = { selectedTab = it }
                )
            }
        }
    } else {
        // Standard Phone layout with bottom NavigationBar
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(1.dp, DarkBorder)
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp)) },
                            label = {
                                Text(
                                    text = item.shortTitle,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = InstrumentCyan,
                                selectedTextColor = InstrumentCyan,
                                indicatorColor = InstrumentCyan.copy(alpha = 0.18f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_${item.shortTitle.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ScreenContent(
                    selectedTab = selectedTab,
                    viewModel = viewModel,
                    locationData = locationData,
                    compassData = compassData,
                    levelData = levelData,
                    pressureData = pressureData,
                    session = session,
                    altimeterSession = altimeterSession,
                    settings = settings,
                    savedLocations = savedLocations,
                    isSpeedLimitExceeded = isSpeedLimitExceeded,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onNavigateToTool = { selectedTab = it }
                )
            }
        }
    }
}

@Composable
private fun ScreenContent(
    selectedTab: Int,
    viewModel: GpsToolkitViewModel,
    locationData: com.example.model.LocationData,
    compassData: com.example.model.CompassData,
    levelData: com.example.model.LevelData,
    pressureData: com.example.model.PressureData,
    session: com.example.model.SpeedometerSession,
    altimeterSession: com.example.model.AltimeterSession,
    settings: com.example.model.AppSettings,
    savedLocations: List<com.example.data.SavedLocation>,
    isSpeedLimitExceeded: Boolean,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onNavigateToTool: (Int) -> Unit
) {
    when (selectedTab) {
        0 -> DashboardScreen(
            locationData = locationData,
            compassData = compassData,
            levelData = levelData,
            settings = settings,
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = onRequestPermission,
            onNavigateToTool = onNavigateToTool
        )
        1 -> SpeedometerScreen(
            locationData = locationData,
            session = session,
            settings = settings,
            isSpeedLimitExceeded = isSpeedLimitExceeded,
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = onRequestPermission,
            onStartTrip = { viewModel.startTripSession() },
            onPauseTrip = { viewModel.pauseTripSession() },
            onResetTrip = { viewModel.resetTripSession() },
            onToggleUnit = { viewModel.updateSpeedUnit(it) }
        )
        2 -> AltimeterScreen(
            locationData = locationData,
            pressureData = pressureData,
            altimeterSession = altimeterSession,
            settings = settings,
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = onRequestPermission,
            onResetBaseline = { viewModel.resetAltimeterBaseline() }
        )
        3 -> CompassScreen(
            compassData = compassData,
            levelData = levelData,
            locationData = locationData,
            settings = settings,
            onToggleNorthReference = { viewModel.updateNorthReference(it) }
        )
        4 -> SpiritLevelScreen(
            levelData = levelData,
            settings = settings,
            onCalibrateZero = { viewModel.calibrateLevel() },
            onResetCalibration = { viewModel.resetLevelCalibration() }
        )
        5 -> CoordinatesScreen(
            locationData = locationData,
            settings = settings,
            savedLocations = savedLocations,
            hasLocationPermission = hasLocationPermission,
            onRequestPermission = onRequestPermission,
            onSaveLocation = { name, note -> viewModel.saveCurrentLocation(name, note) },
            onDeleteLocation = { viewModel.deleteSavedLocation(it) }
        )
        6 -> SettingsScreen(
            settings = settings,
            locationData = locationData,
            compassData = compassData,
            levelData = levelData,
            pressureData = pressureData,
            onUpdateSpeedUnit = { viewModel.updateSpeedUnit(it) },
            onUpdateAltitudeUnit = { viewModel.updateAltitudeUnit(it) },
            onUpdateNorthReference = { viewModel.updateNorthReference(it) },
            onUpdateMaxSpeed = { viewModel.updateMaxGaugeSpeed(it) },
            onUpdateSpeedLimitWarning = { viewModel.updateSpeedLimitWarning(it) },
            onUpdateLevelTolerance = { viewModel.updateLevelTolerance(it) },
            onToggleHaptic = { viewModel.toggleHapticFeedback(it) },
            onToggleKeepScreenOn = { viewModel.toggleKeepScreenOn(it) },
            onUpdateTheme = { viewModel.updateThemeMode(it) }
        )
    }
}

private data class NavigationItem(
    val shortTitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
