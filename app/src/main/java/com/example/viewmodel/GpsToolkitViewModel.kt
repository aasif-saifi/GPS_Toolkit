package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SavedLocation
import com.example.model.AltimeterSession
import com.example.model.AltitudeUnit
import com.example.model.AppThemeMode
import com.example.model.AppSettings
import com.example.model.CompassData
import com.example.model.LevelData
import com.example.model.LocationData
import com.example.model.NorthReference
import com.example.model.PressureData
import com.example.model.SpeedUnit
import com.example.model.SpeedometerSession
import com.example.sensor.GpsLocationTracker
import com.example.sensor.OrientationSensorTracker
import com.example.util.UnitConverter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class GpsToolkitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val locationTracker = GpsLocationTracker(application)
    private val sensorTracker = OrientationSensorTracker(application)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val locationData: StateFlow<LocationData> = locationTracker.locationData
    val compassData: StateFlow<CompassData> = sensorTracker.compassData
    val levelData: StateFlow<LevelData> = sensorTracker.levelData
    val pressureData: StateFlow<PressureData> = sensorTracker.pressureData

    val savedLocations: StateFlow<List<SavedLocation>> = db.savedLocationDao()
        .getAllSavedLocations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _speedometerSession = MutableStateFlow(SpeedometerSession())
    val speedometerSession: StateFlow<SpeedometerSession> = _speedometerSession.asStateFlow()

    private val _altimeterSession = MutableStateFlow(AltimeterSession())
    val altimeterSession: StateFlow<AltimeterSession> = _altimeterSession.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _isSpeedLimitExceeded = MutableStateFlow(false)
    val isSpeedLimitExceeded: StateFlow<Boolean> = _isSpeedLimitExceeded.asStateFlow()

    private var sessionTimerJob: Job? = null
    private var lastRecordedLocation: Location? = null
    private var lastLevelHapticTime = 0L
    private var lastSpeedAlertTime = 0L

    init {
        // Observe location updates for session statistics, altimeter min/max, and compass declination
        viewModelScope.launch {
            locationTracker.locationData.collect { loc ->
                if (loc.isGpsFixed) {
                    sensorTracker.setLocationForDeclination(loc.latitude, loc.longitude, loc.altitudeMeters)
                    updateAltimeterSession(loc.altitudeMeters)
                    checkSpeedLimit(loc.speedMetersPerSec)

                    if (_speedometerSession.value.isRunning && !_speedometerSession.value.isPaused) {
                        recordSessionLocation(loc)
                    }
                }
            }
        }

        // Observe level for haptic feedback when perfectly level
        viewModelScope.launch {
            levelData.collect { level ->
                if (level.isLevel && _settings.value.hapticFeedbackEnabled) {
                    val now = System.currentTimeMillis()
                    if (now - lastLevelHapticTime > 1200L) {
                        lastLevelHapticTime = now
                        vibrateClick(30)
                    }
                }
            }
        }
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _hasLocationPermission.value = granted
        if (granted) {
            locationTracker.startTracking()
            sensorTracker.startListening()
        } else {
            locationTracker.stopTracking()
        }
    }

    fun onResume() {
        if (_hasLocationPermission.value) {
            locationTracker.startTracking()
        }
        sensorTracker.startListening()
    }

    fun onPause() {
        locationTracker.stopTracking()
        sensorTracker.stopListening()
    }

    // --- Speedometer Session Controls ---

    fun startTripSession() {
        if (!_speedometerSession.value.isRunning) {
            _speedometerSession.value = SpeedometerSession(
                isRunning = true,
                isPaused = false,
                startTimeMs = System.currentTimeMillis(),
                elapsedTimeMs = 0L
            )
            startSessionTimer()
        } else if (_speedometerSession.value.isPaused) {
            _speedometerSession.value = _speedometerSession.value.copy(isPaused = false)
            startSessionTimer()
        }
    }

    fun pauseTripSession() {
        _speedometerSession.value = _speedometerSession.value.copy(isPaused = true)
        sessionTimerJob?.cancel()
    }

    fun resetTripSession() {
        sessionTimerJob?.cancel()
        _speedometerSession.value = SpeedometerSession()
        lastRecordedLocation = null
    }

    private fun startSessionTimer() {
        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (isActive && _speedometerSession.value.isRunning && !_speedometerSession.value.isPaused) {
                delay(1000L)
                _speedometerSession.value = _speedometerSession.value.copy(
                    elapsedTimeMs = _speedometerSession.value.elapsedTimeMs + 1000L
                )
            }
        }
    }

    private fun recordSessionLocation(loc: LocationData) {
        val currentLoc = Location("gps").apply {
            latitude = loc.latitude
            longitude = loc.longitude
        }

        var addedDistance = 0.0
        val prev = lastRecordedLocation
        if (prev != null) {
            val dist = prev.distanceTo(currentLoc).toDouble()
            // Avoid GPS drift jitter when standing still
            if (dist > 1.5 && loc.speedMetersPerSec > 0.3f) {
                addedDistance = dist
            }
        }
        lastRecordedLocation = currentLoc

        val currentMax = max(_speedometerSession.value.maxSpeedMps, loc.speedMetersPerSec)
        val currentSum = _speedometerSession.value.totalSpeedSumMps + loc.speedMetersPerSec
        val currentSamples = _speedometerSession.value.speedSamplesCount + 1

        _speedometerSession.value = _speedometerSession.value.copy(
            maxSpeedMps = currentMax,
            totalDistanceMeters = _speedometerSession.value.totalDistanceMeters + addedDistance,
            totalSpeedSumMps = currentSum,
            speedSamplesCount = currentSamples
        )
    }

    private fun checkSpeedLimit(speedMps: Float) {
        val limit = _settings.value.speedLimitWarning
        if (limit <= 0f) {
            _isSpeedLimitExceeded.value = false
            return
        }

        val currentSpeedInUnit = UnitConverter.speedFromMps(speedMps, _settings.value.speedUnit)
        val exceeded = currentSpeedInUnit > limit
        _isSpeedLimitExceeded.value = exceeded

        if (exceeded && _settings.value.hapticFeedbackEnabled) {
            val now = System.currentTimeMillis()
            if (now - lastSpeedAlertTime > 2500L) {
                lastSpeedAlertTime = now
                vibrateClick(150)
            }
        }
    }

    // --- Altimeter Session Controls ---

    private fun updateAltimeterSession(currentAltMeters: Double) {
        val current = _altimeterSession.value
        val baseline = current.baselineAltitudeMeters ?: currentAltMeters
        val newMin = min(current.minAltitudeMeters, currentAltMeters)
        val newMax = max(current.maxAltitudeMeters, currentAltMeters)

        _altimeterSession.value = current.copy(
            baselineAltitudeMeters = baseline,
            minAltitudeMeters = newMin,
            maxAltitudeMeters = newMax
        )
    }

    fun resetAltimeterBaseline() {
        val currentAlt = locationData.value.altitudeMeters
        _altimeterSession.value = _altimeterSession.value.copy(
            baselineAltitudeMeters = currentAlt,
            minAltitudeMeters = currentAlt,
            maxAltitudeMeters = currentAlt
        )
    }

    fun calibrateBarometer(offsetMeters: Float) {
        _altimeterSession.value = _altimeterSession.value.copy(
            pressureOffsetMeters = offsetMeters
        )
    }

    // --- Spirit Level Controls ---

    fun calibrateLevel() {
        sensorTracker.calibrateLevel()
        vibrateClick(40)
    }

    fun resetLevelCalibration() {
        sensorTracker.resetLevelCalibration()
        vibrateClick(40)
    }

    // --- Settings Controls ---

    fun updateSpeedUnit(unit: SpeedUnit) {
        _settings.value = _settings.value.copy(speedUnit = unit)
    }

    fun updateAltitudeUnit(unit: AltitudeUnit) {
        _settings.value = _settings.value.copy(altitudeUnit = unit)
    }

    fun updateNorthReference(ref: NorthReference) {
        _settings.value = _settings.value.copy(northReference = ref)
    }

    fun updateMaxGaugeSpeed(speed: Float) {
        _settings.value = _settings.value.copy(maxGaugeSpeed = speed)
    }

    fun updateSpeedLimitWarning(limit: Float) {
        _settings.value = _settings.value.copy(speedLimitWarning = limit)
    }

    fun updateLevelTolerance(tolerance: Float) {
        _settings.value = _settings.value.copy(levelTolerance = tolerance)
        sensorTracker.setLevelTolerance(tolerance)
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        _settings.value = _settings.value.copy(hapticFeedbackEnabled = enabled)
    }

    fun toggleKeepScreenOn(enabled: Boolean) {
        _settings.value = _settings.value.copy(keepScreenOn = enabled)
    }

    fun updateThemeMode(mode: AppThemeMode) {
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    // --- Saved Locations Management ---

    fun saveCurrentLocation(name: String, note: String = "") {
        val loc = locationData.value
        val entity = SavedLocation(
            name = name.ifBlank { "Waypoint " + System.currentTimeMillis() % 1000 },
            latitude = loc.latitude,
            longitude = loc.longitude,
            altitudeMeters = loc.altitudeMeters,
            address = loc.address ?: "",
            note = note,
            timestampMs = System.currentTimeMillis()
        )
        viewModelScope.launch {
            db.savedLocationDao().insertLocation(entity)
        }
        vibrateClick(50)
    }

    fun deleteSavedLocation(location: SavedLocation) {
        viewModelScope.launch {
            db.savedLocationDao().deleteLocation(location)
        }
    }

    fun clearAllSavedLocations() {
        viewModelScope.launch {
            db.savedLocationDao().clearAll()
        }
    }

    private fun vibrateClick(durationMs: Long) {
        if (!_settings.value.hapticFeedbackEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationTracker.stopTracking()
        sensorTracker.stopListening()
        sessionTimerJob?.cancel()
    }
}
