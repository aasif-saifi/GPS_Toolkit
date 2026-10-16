package com.example

import com.example.model.AltitudeUnit
import com.example.model.LocationData
import com.example.model.SpeedUnit
import com.example.model.SpeedometerSession
import com.example.util.SensorFilter
import com.example.util.UnitConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testSpeedConversion_mpsToKmh() {
        val mps = 10f // 10 m/s = 36 km/h
        val kmh = UnitConverter.speedFromMps(mps, SpeedUnit.KMH)
        assertEquals(36.0f, kmh, 0.01f)
    }

    @Test
    fun testSpeedConversion_mpsToMph() {
        val mps = 10f // 10 m/s ~ 22.369 mph
        val mph = UnitConverter.speedFromMps(mps, SpeedUnit.MPH)
        assertEquals(22.369f, mph, 0.01f)
    }

    @Test
    fun testSpeedConversion_mpsToKnots() {
        val mps = 10f // 10 m/s ~ 19.438 knots
        val knots = UnitConverter.speedFromMps(mps, SpeedUnit.KNOTS)
        assertEquals(19.438f, knots, 0.01f)
    }

    @Test
    fun testSpeedConversion_negativeOrNan_returnsZero() {
        assertEquals(0f, UnitConverter.speedFromMps(-5f, SpeedUnit.KMH), 0.001f)
        assertEquals(0f, UnitConverter.speedFromMps(Float.NaN, SpeedUnit.KMH), 0.001f)
    }

    @Test
    fun testAltitudeConversion_metersToFeet() {
        val meters = 100.0 // 100 m ~ 328.084 ft
        val feet = UnitConverter.altitudeFromMeters(meters, AltitudeUnit.FEET)
        assertEquals(328.084, feet, 0.01)
    }

    @Test
    fun testHeadingNormalization() {
        assertEquals(0f, UnitConverter.normalizeHeading(0f), 0.001f)
        assertEquals(45f, UnitConverter.normalizeHeading(405f), 0.001f)
        assertEquals(350f, UnitConverter.normalizeHeading(-10f), 0.001f)
        assertEquals(180f, UnitConverter.normalizeHeading(-540f), 0.001f)
    }

    @Test
    fun testCardinalHeadings() {
        assertEquals("N", UnitConverter.headingToCardinal(0f))
        assertEquals("N", UnitConverter.headingToCardinal(359f))
        assertEquals("N", UnitConverter.headingToCardinal(10f))
        assertEquals("NE", UnitConverter.headingToCardinal(45f))
        assertEquals("E", UnitConverter.headingToCardinal(90f))
        assertEquals("SE", UnitConverter.headingToCardinal(135f))
        assertEquals("S", UnitConverter.headingToCardinal(180f))
        assertEquals("SW", UnitConverter.headingToCardinal(225f))
        assertEquals("W", UnitConverter.headingToCardinal(270f))
        assertEquals("NW", UnitConverter.headingToCardinal(315f))
    }

    @Test
    fun testDmsConversion() {
        // Latitude 37.332981 N
        val latDms = UnitConverter.toDms(37.332981, true)
        assertTrue(latDms.contains("37°"))
        assertTrue(latDms.endsWith("N"))

        // Longitude -122.053543 W
        val lngDms = UnitConverter.toDms(-122.053543, false)
        assertTrue(lngDms.contains("122°"))
        assertTrue(lngDms.endsWith("W"))
    }

    @Test
    fun testBarometricAltitudeFormula() {
        // At standard sea level pressure 1013.25 hPa, altitude is ~0m
        val seaLevelAlt = UnitConverter.calculateBarometricAltitude(1013.25f)
        assertEquals(0f, seaLevelAlt, 1.0f)

        // Lower pressure means higher altitude
        val highAlt = UnitConverter.calculateBarometricAltitude(800.0f)
        assertTrue("Pressure at 800 hPa should correspond to elevation > 1800m", highAlt > 1800f)
    }

    @Test
    fun testElapsedTimeFormatting() {
        val ms = 3665000L // 1 hour, 1 minute, 5 seconds
        assertEquals("01:01:05", UnitConverter.formatElapsedTime(ms))
    }

    @Test
    fun testDistanceFormatting() {
        val shortDist = 450.0 // 450 meters
        assertEquals("450 m", UnitConverter.formatDistance(shortDist, SpeedUnit.KMH))

        val longDist = 12500.0 // 12.5 km
        assertEquals("12.50 km", UnitConverter.formatDistance(longDist, SpeedUnit.KMH))

        val milesDist = 16093.44 // ~ 10 miles
        assertEquals("10.00 mi", UnitConverter.formatDistance(milesDist, SpeedUnit.MPH))
    }

    @Test
    fun testSensorFilter_angleSmoothing() {
        // Smooth transition across 0/360 boundary
        val smoothed = SensorFilter.filterAngle(359f, 2f, 0.5f)
        // Should be close to 0° / 360°, not jumping to 180°
        assertTrue("Angle smoothing across 0 boundary should be near 0/360 but got $smoothed", smoothed < 10f || smoothed > 350f)
    }

    @Test
    fun testSpeedometerSession_averageSpeed() {
        val session = SpeedometerSession(
            speedSamplesCount = 4,
            totalSpeedSumMps = 40.0
        )
        assertEquals(10f, session.averageSpeedMps, 0.001f)
    }
}
