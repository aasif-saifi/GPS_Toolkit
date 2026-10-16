package com.example.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import com.example.model.LocationData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class GpsLocationTracker(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _locationData = MutableStateFlow(LocationData())
    val locationData: StateFlow<LocationData> = _locationData.asStateFlow()

    private var isTracking = false
    private var lastLocation: Location? = null
    private var lastGeocodeTime = 0L

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            processLocation(location)
        }
    }

    private val fallbackListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            processLocation(location)
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (isTracking) return
        isTracking = true

        try {
            // Fused Location Provider with high accuracy for speedometer & navigation
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .setMinUpdateDistanceMeters(0f)
                .build()

            fusedClient.requestLocationUpdates(locationRequest, fusedCallback, Looper.getMainLooper())
                .addOnFailureListener {
                    startFallbackLocationManager()
                }

            // Also check last known location immediately
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && _locationData.value.timestampMs == 0L) {
                    processLocation(loc)
                }
            }
        } catch (_: SecurityException) {
            startFallbackLocationManager()
        } catch (_: Exception) {
            startFallbackLocationManager()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startFallbackLocationManager() {
        try {
            locationManager?.let { lm ->
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, fallbackListener, Looper.getMainLooper())
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 0f, fallbackListener, Looper.getMainLooper())
                }
            }
        } catch (_: Exception) {
            // Handled gracefully
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        try {
            fusedClient.removeLocationUpdates(fusedCallback)
            locationManager?.removeUpdates(fallbackListener)
        } catch (_: Exception) {
        }
    }

    private fun processLocation(location: Location) {
        // Clean speed jitter: stationary GPS flutter (< 0.28 m/s ~ 1 km/h) clamped to 0
        val rawSpeed = if (location.hasSpeed()) location.speed else 0f
        val filteredSpeed = if (rawSpeed < 0.28f) 0f else rawSpeed

        val verticalAccuracy: Float? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasVerticalAccuracy()) {
            location.verticalAccuracyMeters
        } else null

        val newData = LocationData(
            latitude = location.latitude,
            longitude = location.longitude,
            altitudeMeters = location.altitude,
            speedMetersPerSec = filteredSpeed,
            bearingDegrees = if (location.hasBearing()) location.bearing else _locationData.value.bearingDegrees,
            horizontalAccuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f,
            verticalAccuracyMeters = verticalAccuracy,
            timestampMs = location.time,
            provider = location.provider ?: "GPS",
            isGpsFixed = true,
            address = _locationData.value.address
        )

        lastLocation = location
        _locationData.value = newData

        // Periodically reverse-geocode (throttle to once every 15 seconds)
        val now = SystemClock.elapsedRealtime()
        if (now - lastGeocodeTime > 15000L) {
            lastGeocodeTime = now
            reverseGeocode(location.latitude, location.longitude)
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        scope.launch {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addressString = withContext(Dispatchers.IO) {
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val locality = addr.locality ?: addr.subAdminArea ?: ""
                        val adminArea = addr.adminArea ?: ""
                        val country = addr.countryName ?: ""
                        val thoroughfare = addr.thoroughfare
                        val subThoroughfare = addr.subThoroughfare

                        buildString {
                            if (thoroughfare != null) {
                                if (subThoroughfare != null) append("$subThoroughfare ")
                                append("$thoroughfare, ")
                            }
                            if (locality.isNotEmpty()) append("$locality, ")
                            if (adminArea.isNotEmpty()) append("$adminArea, ")
                            if (country.isNotEmpty()) append(country)
                        }.trimEnd(',', ' ')
                    } else null
                }
                if (addressString != null) {
                    _locationData.value = _locationData.value.copy(address = addressString)
                }
            } catch (_: Exception) {
                // Network unavailable for geocoding
            }
        }
    }
}
