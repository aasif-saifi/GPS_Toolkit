package com.example.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Filter utility for smooth, jitter-free sensor and compass rendering
 */
object SensorFilter {

    /**
     * Circular low-pass filter for angular azimuths [0, 360) degrees
     * Avoids wrap-around discontinuities (e.g. 359° -> 1°)
     */
    fun filterAngle(currentAngle: Float, targetAngle: Float, alpha: Float = 0.15f): Float {
        val curRad = Math.toRadians(currentAngle.toDouble())
        val targetRad = Math.toRadians(targetAngle.toDouble())

        val curX = cos(curRad)
        val curY = sin(curRad)

        val targetX = cos(targetRad)
        val targetY = sin(targetRad)

        val smoothX = curX + alpha * (targetX - curX)
        val smoothY = curY + alpha * (targetY - curY)

        var resultDeg = Math.toDegrees(atan2(smoothY, smoothX)).toFloat()
        if (resultDeg < 0f) resultDeg += 360f
        return resultDeg
    }

    /**
     * Standard Exponential Moving Average (EMA) low-pass filter
     */
    fun filterLinear(current: Float, target: Float, alpha: Float = 0.2f): Float {
        return current + alpha * (target - current)
    }

    fun filterLinear(current: Double, target: Double, alpha: Double = 0.2): Double {
        return current + alpha * (target - current)
    }
}
