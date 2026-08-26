package com.example.data.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.example.domain.detection.SensorTelemetry
import com.example.domain.model.LocationPoint
import com.example.domain.model.MovementState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Raw hardware sensor snapshot for high-rate sliding window buffering.
 */
data class RawSensorSample(
    val ax: Float,
    val ay: Float,
    val az: Float,
    val gx: Float,
    val gy: Float,
    val gz: Float,
    val timestamp: Long
)

enum class GpsStatus {
    INITIALIZING,
    ACTIVE,
    DISABLED,
    PERMISSION_DENIED,
    NO_FIX
}

/**
 * SensorPipeline: Manages hardware accelerometer, gyroscope, and GPS streams.
 * Performs ~20-50Hz sliding buffer normalization, feature extraction, and emits clean telemetry.
 */
class SensorPipeline(
    private val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener, LocationListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Sliding window buffer (keeps last 60 samples ~ 1.5 - 3 seconds at 20-40Hz)
    private val sampleBuffer = ConcurrentLinkedQueue<RawSensorSample>()
    private val maxBufferSize = 60

    // Latest Raw readings
    private var curAx = 0f
    private var curAy = 0f
    private var curAz = 9.80665f
    private var curGx = 0f
    private var curGy = 0f
    private var curGz = 0f

    // GPS & Motion state
    private var lastLocation: Location? = null
    private var prevSpeedKmh = 0f
    private var curSpeedKmh = 0f
    private var curHeading = 0f
    private var lastGpsTimestamp = 0L

    private val _telemetry = MutableStateFlow(SensorTelemetry())
    val telemetry: StateFlow<SensorTelemetry> = _telemetry.asStateFlow()

    private val _gpsStatus = MutableStateFlow(GpsStatus.INITIALIZING)
    val gpsStatus: StateFlow<GpsStatus> = _gpsStatus.asStateFlow()

    private val _hasHardwareSensors = MutableStateFlow(accelerometer != null)
    val hasHardwareSensors: StateFlow<Boolean> = _hasHardwareSensors.asStateFlow()

    private val _sampledWaypoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val sampledWaypoints: StateFlow<List<LocationPoint>> = _sampledWaypoints.asStateFlow()

    private var isTracking = false
    private var lastWaypointTime = 0L

    fun startTracking() {
        if (isTracking) return
        isTracking = true

        sampleBuffer.clear()
        _sampledWaypoints.value = emptyList()

        // Register sensors with battery-aware ~20Hz (SENSOR_DELAY_UI / 50000 microsecs or GAME)
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        requestGpsUpdates()
    }

    @SuppressLint("MissingPermission")
    fun requestGpsUpdates() {
        try {
            val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            val isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

            if (!isGpsEnabled && !isNetworkEnabled) {
                _gpsStatus.value = GpsStatus.DISABLED
                return
            }

            _gpsStatus.value = GpsStatus.ACTIVE

            // Get last known location for instant fix
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastGps?.let { onLocationChanged(it) }

            // Request periodic updates (min 2 seconds or 5 meters)
            if (isGpsEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    3f,
                    this
                )
            }
            if (isNetworkEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    5f,
                    this
                )
            }
        } catch (e: SecurityException) {
            _gpsStatus.value = GpsStatus.PERMISSION_DENIED
        } catch (e: Exception) {
            _gpsStatus.value = GpsStatus.NO_FIX
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false

        sensorManager?.unregisterListener(this)
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            // Ignored
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isTracking) return

        val now = System.currentTimeMillis()
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                curAx = event.values[0]
                curAy = event.values[1]
                curAz = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                curGx = event.values[0]
                curGy = event.values[1]
                curGz = event.values[2]
            }
        }

        // Add to sliding circular buffer
        val sample = RawSensorSample(curAx, curAy, curAz, curGx, curGy, curGz, now)
        sampleBuffer.add(sample)
        while (sampleBuffer.size > maxBufferSize) {
            sampleBuffer.poll()
        }

        // Process features and emit
        processAndEmitTelemetry(now)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        val now = System.currentTimeMillis()

        // Speed from GPS (m/s to km/h)
        val speedKmh = if (location.hasSpeed()) {
            location.speed * 3.6f
        } else {
            curSpeedKmh
        }

        val speedDelta = if (lastGpsTimestamp > 0 && now - lastGpsTimestamp in 500..5000) {
            prevSpeedKmh - speedKmh
        } else {
            0f
        }

        prevSpeedKmh = curSpeedKmh
        curSpeedKmh = speedKmh
        curHeading = if (location.hasBearing()) location.bearing else curHeading
        lastGpsTimestamp = now
        _gpsStatus.value = GpsStatus.ACTIVE

        // Sample route points every 5 seconds or significant movement
        if (now - lastWaypointTime >= 4000L) {
            lastWaypointTime = now
            val wp = LocationPoint(
                latitude = location.latitude,
                longitude = location.longitude,
                speedKmh = speedKmh,
                headingDegrees = curHeading,
                timestamp = now
            )
            val currentList = _sampledWaypoints.value.toMutableList()
            currentList.add(wp)
            _sampledWaypoints.value = currentList
        }

        processAndEmitTelemetry(now)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {
        _gpsStatus.value = GpsStatus.ACTIVE
    }

    override fun onProviderDisabled(provider: String) {
        _gpsStatus.value = GpsStatus.DISABLED
    }

    private fun processAndEmitTelemetry(timestamp: Long) {
        val totalGForce = SensorTelemetry.computeGForce(curAx, curAy, curAz)
        val rotationDps = SensorTelemetry.computeRotationRateDps(curGx, curGy, curGz)

        // Jerk calculation from sliding window
        val jerk = calculateJerkFromBuffer()

        // Movement state classification
        val movementState = when {
            totalGForce >= 3.8f -> MovementState.ABNORMAL_IMPACT
            curSpeedKmh < 3f -> MovementState.STATIONARY
            prevSpeedKmh - curSpeedKmh >= 25f -> MovementState.HARD_BRAKING
            prevSpeedKmh - curSpeedKmh >= 10f -> MovementState.DECELERATING
            rotationDps >= 60f -> MovementState.TURNING
            else -> MovementState.CRUISING
        }

        val updated = SensorTelemetry(
            accelerometerX = curAx,
            accelerometerY = curAy,
            accelerometerZ = curAz,
            totalGForce = totalGForce,
            gyroscopeX = curGx,
            gyroscopeY = curGy,
            gyroscopeZ = curGz,
            rotationRateDps = rotationDps,
            jerkGPerSec = jerk,
            speedKmh = curSpeedKmh,
            speedDeltaKmh = (prevSpeedKmh - curSpeedKmh).coerceAtLeast(0f),
            headingDegrees = curHeading,
            latitude = lastLocation?.latitude ?: 12.9716,
            longitude = lastLocation?.longitude ?: 77.5946,
            altitudeMeters = lastLocation?.altitude,
            accuracyMeters = lastLocation?.accuracy ?: 8.0f,
            movementState = movementState,
            timestamp = timestamp
        )

        _telemetry.value = updated
    }

    private fun calculateJerkFromBuffer(): Float {
        val list = sampleBuffer.toList()
        if (list.size < 2) return 0f
        val latest = list.last()
        val prev = list[list.size - 2]
        val dt = (latest.timestamp - prev.timestamp) / 1000f
        if (dt <= 0.001f) return 0f

        val gLatest = SensorTelemetry.computeGForce(latest.ax, latest.ay, latest.az)
        val gPrev = SensorTelemetry.computeGForce(prev.ax, prev.ay, prev.az)
        return abs(gLatest - gPrev) / dt
    }

    /**
     * Injects synthetic simulation telemetry for interactive testing in emulator or field preview.
     */
    fun injectTelemetry(telemetry: SensorTelemetry) {
        curAx = telemetry.accelerometerX
        curAy = telemetry.accelerometerY
        curAz = telemetry.accelerometerZ
        curGx = telemetry.gyroscopeX
        curGy = telemetry.gyroscopeY
        curGz = telemetry.gyroscopeZ
        curSpeedKmh = telemetry.speedKmh
        curHeading = telemetry.headingDegrees
        _telemetry.value = telemetry

        // Record simulated waypoint
        telemetry.latitude?.let { lat ->
            telemetry.longitude?.let { lng ->
                val wp = LocationPoint(
                    latitude = lat,
                    longitude = lng,
                    speedKmh = telemetry.speedKmh,
                    headingDegrees = telemetry.headingDegrees,
                    timestamp = telemetry.timestamp
                )
                val currentList = _sampledWaypoints.value.toMutableList()
                currentList.add(wp)
                _sampledWaypoints.value = currentList
            }
        }
    }
}
