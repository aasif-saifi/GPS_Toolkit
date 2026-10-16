package com.example.util

import com.example.model.AltitudeUnit
import com.example.model.SpeedUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow

object UnitConverter {

    private const val MPS_TO_KMH = 3.6f
    private const val MPS_TO_MPH = 2.2369363f
    private const val MPS_TO_KNOTS = 1.9438445f
    private const val METERS_TO_FEET = 3.2808399

    /**
     * Convert meters per second to the requested SpeedUnit
     */
    fun speedFromMps(mps: Float, unit: SpeedUnit): Float {
        if (mps.isNaN() || mps < 0f) return 0f
        return when (unit) {
            SpeedUnit.KMH -> mps * MPS_TO_KMH
            SpeedUnit.MPH -> mps * MPS_TO_MPH
            SpeedUnit.KNOTS -> mps * MPS_TO_KNOTS
            SpeedUnit.MS -> mps
        }
    }

    /**
     * Convert speed from specified SpeedUnit back to m/s
     */
    fun speedToMps(value: Float, unit: SpeedUnit): Float {
        if (value <= 0f) return 0f
        return when (unit) {
            SpeedUnit.KMH -> value / MPS_TO_KMH
            SpeedUnit.MPH -> value / MPS_TO_MPH
            SpeedUnit.KNOTS -> value / MPS_TO_KNOTS
            SpeedUnit.MS -> value
        }
    }

    /**
     * Convert altitude in meters to requested AltitudeUnit
     */
    fun altitudeFromMeters(meters: Double, unit: AltitudeUnit): Double {
        return when (unit) {
            AltitudeUnit.METERS -> meters
            AltitudeUnit.FEET -> meters * METERS_TO_FEET
        }
    }

    /**
     * Convert distance in meters to formatted string according to unit system
     */
    fun formatDistance(meters: Double, speedUnit: SpeedUnit): String {
        return when (speedUnit) {
            SpeedUnit.MPH -> {
                val miles = meters * 0.000621371
                if (miles < 0.1) {
                    val feet = meters * METERS_TO_FEET
                    String.format(Locale.US, "%.0f ft", feet)
                } else {
                    String.format(Locale.US, "%.2f mi", miles)
                }
            }
            SpeedUnit.KNOTS -> {
                val nm = meters * 0.000539957
                String.format(Locale.US, "%.2f NM", nm)
            }
            else -> {
                if (meters < 1000.0) {
                    String.format(Locale.US, "%.0f m", meters)
                } else {
                    val km = meters / 1000.0
                    String.format(Locale.US, "%.2f km", km)
                }
            }
        }
    }

    /**
     * Normalize heading azimuth to [0, 360) range
     */
    fun normalizeHeading(degrees: Float): Float {
        var d = degrees % 360f
        if (d < 0f) d += 360f
        return d
    }

    /**
     * Convert azimuth degrees to 8-point cardinal abbreviation
     */
    fun headingToCardinal(degrees: Float): String {
        val norm = normalizeHeading(degrees)
        return when (norm) {
            in 22.5f..67.5f -> "NE"
            in 67.5f..112.5f -> "E"
            in 112.5f..157.5f -> "SE"
            in 157.5f..202.5f -> "S"
            in 202.5f..247.5f -> "SW"
            in 247.5f..292.5f -> "W"
            in 292.5f..337.5f -> "NW"
            else -> "N"
        }
    }

    /**
     * Convert decimal degrees coordinate to DMS format string
     * e.g. 35°43'14" N or 15°10'24" E
     */
    fun toDms(decimalDegrees: Double, isLatitude: Boolean): String {
        val absolute = abs(decimalDegrees)
        val degrees = absolute.toInt()
        val remainderMinutes = (absolute - degrees) * 60.0
        val minutes = remainderMinutes.toInt()
        val seconds = (remainderMinutes - minutes) * 60.0

        val direction = if (isLatitude) {
            if (decimalDegrees >= 0) "N" else "S"
        } else {
            if (decimalDegrees >= 0) "E" else "W"
        }

        return String.format(Locale.US, "%d°%02d'%02.0f\" %s", degrees, minutes, seconds, direction)
    }

    /**
     * Calculate barometric altitude in meters using standard hypsometric formula
     * reference: standard sea-level pressure 1013.25 hPa
     */
    fun calculateBarometricAltitude(pressureHpa: Float, seaLevelHpa: Float = 1013.25f): Float {
        if (pressureHpa <= 0f) return 0f
        return (44330.0 * (1.0 - (pressureHpa / seaLevelHpa).toDouble().pow(1.0 / 5.255))).toFloat()
    }

    /**
     * Format milliseconds elapsed to HH:mm:ss
     */
    fun formatElapsedTime(elapsedMs: Long): String {
        val seconds = (elapsedMs / 1000) % 60
        val minutes = (elapsedMs / (1000 * 60)) % 60
        val hours = elapsedMs / (1000 * 60 * 60)
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
