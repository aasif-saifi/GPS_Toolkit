package com.example.sensor

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import com.example.model.CompassData
import com.example.model.LevelData
import com.example.model.PressureData
import com.example.util.SensorFilter
import com.example.util.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class OrientationSensorTracker(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)

    private val _compassData = MutableStateFlow(
        CompassData(hasCompassSensor = rotationVectorSensor != null || (accelerometerSensor != null && magneticSensor != null))
    )
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()

    private val _levelData = MutableStateFlow(
        LevelData(hasAccelerometer = rotationVectorSensor != null || accelerometerSensor != null)
    )
    val levelData: StateFlow<LevelData> = _levelData.asStateFlow()

    private val _pressureData = MutableStateFlow(
        PressureData(isAvailable = pressureSensor != null)
    )
    val pressureData: StateFlow<PressureData> = _pressureData.asStateFlow()

    private var isListening = false

    // Fallback sensor caches
    private val gravityValues = FloatArray(3)
    private val geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    // Level calibration offsets
    private var pitchOffset = 0f
    private var rollOffset = 0f
    private var levelTolerance = 1.0f

    // Location coordinates for True North declination calculation
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentAltitude = 0.0
    private var hasLocationForDeclination = false

    fun setLocationForDeclination(latitude: Double, longitude: Double, altitude: Double) {
        currentLatitude = latitude
        currentLongitude = longitude
        currentAltitude = altitude
        hasLocationForDeclination = true
    }

    fun setLevelTolerance(tolerance: Float) {
        levelTolerance = tolerance
    }

    fun calibrateLevel() {
        pitchOffset = _levelData.value.pitchDegrees + pitchOffset
        rollOffset = _levelData.value.rollDegrees + rollOffset
        updateLevelData(0f, 0f)
    }

    fun resetLevelCalibration() {
        pitchOffset = 0f
        rollOffset = 0f
    }

    fun startListening() {
        if (isListening || sensorManager == null) return
        isListening = true

        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            magneticSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }

        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        isListening = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                processRotationVector(event.values, event.accuracy)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravityValues, 0, 3)
                hasGravity = true
                if (hasGeomagnetic) {
                    processAccelerometerAndMagnetic(event.accuracy)
                } else {
                    // Update pitch & roll using accelerometer directly if magnetometer missing
                    processAccelerometerOnly(gravityValues)
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagneticValues, 0, 3)
                hasGeomagnetic = true
                calculateMagneticFieldStrength(event.values)
                if (hasGravity) {
                    processAccelerometerAndMagnetic(event.accuracy)
                }
            }
            Sensor.TYPE_PRESSURE -> {
                val pressure = event.values[0]
                val altitude = UnitConverter.calculateBarometricAltitude(pressure)
                _pressureData.value = PressureData(
                    pressureHpa = pressure,
                    barometricAltitudeMeters = altitude,
                    isAvailable = true
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_ROTATION_VECTOR || sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            _compassData.value = _compassData.value.copy(accuracy = accuracy)
        }
    }

    private fun processRotationVector(rotationVector: FloatArray, sensorAccuracy: Int) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        remapAndExtractOrientation(rotationMatrix, sensorAccuracy)
    }

    private fun processAccelerometerAndMagnetic(sensorAccuracy: Int) {
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityValues, geomagneticValues)
        if (success) {
            remapAndExtractOrientation(rotationMatrix, sensorAccuracy)
        }
    }

    private fun processAccelerometerOnly(gravity: FloatArray) {
        val ax = gravity[0]
        val ay = gravity[1]
        val az = gravity[2]

        val pitch = Math.toDegrees(kotlin.math.atan2(-ay.toDouble(), kotlin.math.sqrt((ax * ax + az * az).toDouble()))).toFloat()
        val roll = Math.toDegrees(kotlin.math.atan2(ax.toDouble(), az.toDouble())).toFloat()

        updateLevelData(pitch, roll)
    }

    private fun remapAndExtractOrientation(rotationMatrix: FloatArray, sensorAccuracy: Int) {
        val remappedMatrix = FloatArray(9)
        val displayRotation = windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0

        var axisX = SensorManager.AXIS_X
        var axisY = SensorManager.AXIS_Y

        when (displayRotation) {
            Surface.ROTATION_90 -> {
                axisX = SensorManager.AXIS_Y
                axisY = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                axisX = SensorManager.AXIS_MINUS_X
                axisY = SensorManager.AXIS_MINUS_Y
            }
            Surface.ROTATION_270 -> {
                axisX = SensorManager.AXIS_MINUS_Y
                axisY = SensorManager.AXIS_X
            }
            else -> {
                axisX = SensorManager.AXIS_X
                axisY = SensorManager.AXIS_Y
            }
        }

        SensorManager.remapCoordinateSystem(rotationMatrix, axisX, axisY, remappedMatrix)

        val orientation = FloatArray(3)
        SensorManager.getOrientation(remappedMatrix, orientation)

        var rawAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        if (rawAzimuth < 0f) rawAzimuth += 360f

        val rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        // Smooth azimuth using circular filter
        val currentAzimuth = _compassData.value.azimuthDegrees
        val smoothedAzimuth = SensorFilter.filterAngle(currentAzimuth, rawAzimuth, 0.18f)

        // Calculate True North declination if coordinates are available
        var declination = 0f
        var trueAzimuth = smoothedAzimuth
        var trueNorthAvailable = false

        if (hasLocationForDeclination && currentLatitude != 0.0) {
            try {
                val geoField = GeomagneticField(
                    currentLatitude.toFloat(),
                    currentLongitude.toFloat(),
                    currentAltitude.toFloat(),
                    System.currentTimeMillis()
                )
                declination = geoField.declination
                trueAzimuth = UnitConverter.normalizeHeading(smoothedAzimuth + declination)
                trueNorthAvailable = true
            } catch (_: Exception) {
            }
        }

        _compassData.value = _compassData.value.copy(
            azimuthDegrees = smoothedAzimuth,
            magneticAzimuth = smoothedAzimuth,
            trueAzimuth = trueAzimuth,
            declinationDegrees = declination,
            pitchDegrees = rawPitch,
            rollDegrees = rawRoll,
            accuracy = sensorAccuracy,
            isTrueNorthAvailable = trueNorthAvailable,
            hasCompassSensor = true
        )

        updateLevelData(rawPitch, rawRoll)
    }

    private fun calculateMagneticFieldStrength(values: FloatArray) {
        val bx = values[0]
        val by = values[1]
        val bz = values[2]
        val magnitude = sqrt((bx * bx + by * by + bz * bz).toDouble()).toFloat()
        _compassData.value = _compassData.value.copy(magneticFieldMicroTesla = magnitude)
    }

    private fun updateLevelData(rawPitch: Float, rawRoll: Float) {
        val effectivePitch = rawPitch - pitchOffset
        val effectiveRoll = rawRoll - rollOffset

        // Smooth pitch and roll
        val currentPitch = _levelData.value.pitchDegrees
        val currentRoll = _levelData.value.rollDegrees
        val smoothPitch = SensorFilter.filterLinear(currentPitch, effectivePitch, 0.25f)
        val smoothRoll = SensorFilter.filterLinear(currentRoll, effectiveRoll, 0.25f)

        val isLevel = abs(smoothPitch) <= levelTolerance && abs(smoothRoll) <= levelTolerance

        _levelData.value = LevelData(
            pitchDegrees = smoothPitch,
            rollDegrees = smoothRoll,
            calibratedPitchOffset = pitchOffset,
            calibratedRollOffset = rollOffset,
            isLevel = isLevel,
            isCalibrated = pitchOffset != 0f || rollOffset != 0f,
            hasAccelerometer = true
        )
    }
}
