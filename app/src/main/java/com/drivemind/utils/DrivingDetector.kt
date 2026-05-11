package com.drivemind.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects whether the user is driving based on GPS speed.
 *
 * Thresholds:
 *  - DRIVING:  speed > 15 km/h for 10+ seconds
 *  - STOPPED:  speed < 5 km/h for 30+ seconds
 *
 * Hysteresis prevents rapid mode switching.
 */
@Singleton
class DrivingDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    enum class DrivingState { UNKNOWN, PARKED, DRIVING }

    private val _drivingState = MutableStateFlow(DrivingState.UNKNOWN)
    val drivingState: StateFlow<DrivingState> = _drivingState.asStateFlow()

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh: StateFlow<Float> = _speedKmh.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    private var drivingConfirmationCount = 0
    private var parkedConfirmationCount = 0

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        UPDATE_INTERVAL_MS
    ).apply {
        setMinUpdateIntervalMillis(FASTEST_UPDATE_MS)
        setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
    }.build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { processLocation(it) }
        }
    }

    @SuppressLint("MissingPermission")
    fun startDetection() {
        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopDetection() {
        fusedClient.removeLocationUpdates(locationCallback)
        _drivingState.value = DrivingState.UNKNOWN
        drivingConfirmationCount = 0
        parkedConfirmationCount = 0
    }

    private fun processLocation(location: Location) {
        if (!location.hasSpeed()) return

        val speedMs = location.speed
        val speedKmh = speedMs * 3.6f
        _speedKmh.value = speedKmh

        when {
            speedKmh > DRIVING_SPEED_THRESHOLD_KMH -> {
                drivingConfirmationCount++
                parkedConfirmationCount = 0
                if (drivingConfirmationCount >= CONFIRMATIONS_NEEDED &&
                    _drivingState.value != DrivingState.DRIVING) {
                    _drivingState.value = DrivingState.DRIVING
                }
            }

            speedKmh < PARKED_SPEED_THRESHOLD_KMH -> {
                parkedConfirmationCount++
                drivingConfirmationCount = 0
                if (parkedConfirmationCount >= CONFIRMATIONS_NEEDED &&
                    _drivingState.value != DrivingState.PARKED) {
                    _drivingState.value = DrivingState.PARKED
                }
            }

            else -> {
                // Speed in uncertain zone — don't change state, reset counters slightly
                drivingConfirmationCount = maxOf(0, drivingConfirmationCount - 1)
                parkedConfirmationCount = maxOf(0, parkedConfirmationCount - 1)
            }
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_MS = 5_000L
        private const val FASTEST_UPDATE_MS = 2_000L
        private const val MIN_DISTANCE_METERS = 10f
        private const val DRIVING_SPEED_THRESHOLD_KMH = 15f
        private const val PARKED_SPEED_THRESHOLD_KMH = 5f
        private const val CONFIRMATIONS_NEEDED = 3
    }
}
