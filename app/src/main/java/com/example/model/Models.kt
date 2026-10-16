package com.example.model

/**
 * GPS and Network Location data snapshot
 */
data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitudeMeters: Double = 0.0,
    val speedMetersPerSec: Float = 0f,
    val bearingDegrees: Float = 0f,
    val horizontalAccuracyMeters: Float = 0f,
    val verticalAccuracyMeters: Float? = null,
    val timestampMs: Long = 0L,
    val provider: String = "GPS",
    val isGpsFixed: Boolean = false,
    val address: String? = null
)

/**
 * Orientation and Magnetic sensor readings
 */
data class CompassData(
    val azimuthDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val magneticAzimuth: Float = 0f,
    val trueAzimuth: Float = 0f,
    val declinationDegrees: Float = 0f,
    val magneticFieldMicroTesla: Float = 0f,
    val accuracy: Int = 3, // SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    val isTrueNorthAvailable: Boolean = false,
    val hasCompassSensor: Boolean = true
)

/**
 * Spirit / Bubble level readings
 */
data class LevelData(
    val pitchDegrees: Float = 0f,
    val rollDegrees: Float = 0f,
    val calibratedPitchOffset: Float = 0f,
    val calibratedRollOffset: Float = 0f,
    val isLevel: Boolean = false,
    val isCalibrated: Boolean = false,
    val hasAccelerometer: Boolean = true
)

/**
 * Barometer / Pressure readings
 */
data class PressureData(
    val pressureHpa: Float = 1013.25f,
    val barometricAltitudeMeters: Float = 0f,
    val isAvailable: Boolean = false
)

/**
 * Speedometer Trip Session statistics
 */
data class SpeedometerSession(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val startTimeMs: Long = 0L,
    val elapsedTimeMs: Long = 0L,
    val maxSpeedMps: Float = 0f,
    val totalDistanceMeters: Double = 0.0,
    val speedSamplesCount: Int = 0,
    val totalSpeedSumMps: Double = 0.0
) {
    val averageSpeedMps: Float
        get() = if (speedSamplesCount > 0) (totalSpeedSumMps / speedSamplesCount).toFloat() else 0f
}

/**
 * Altimeter session tracking
 */
data class AltimeterSession(
    val baselineAltitudeMeters: Double? = null,
    val minAltitudeMeters: Double = Double.MAX_VALUE,
    val maxAltitudeMeters: Double = Double.MIN_VALUE,
    val pressureOffsetMeters: Float = 0f
)

/**
 * User Preferences & Calibration settings
 */
data class AppSettings(
    val speedUnit: SpeedUnit = SpeedUnit.KMH,
    val altitudeUnit: AltitudeUnit = AltitudeUnit.METERS,
    val northReference: NorthReference = NorthReference.MAGNETIC,
    val maxGaugeSpeed: Float = 180f,
    val speedLimitWarning: Float = 0f, // 0 = off, else in speedUnit
    val levelTolerance: Float = 1.0f, // degrees (+/- tolerance)
    val hapticFeedbackEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.DARK
)
