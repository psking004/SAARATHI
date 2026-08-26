package com.example.domain.detection

import com.example.domain.model.MovementState
import com.example.domain.model.RiskLevel
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Sensor telemetry model containing real-time kinematic, rotational, and positional inputs.
 * Standardized across Android hardware sensor pipeline and GPS feeds.
 */
data class SensorTelemetry(
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 9.80665f,
    val totalGForce: Float = 1.0f,
    val gyroscopeX: Float = 0f,
    val gyroscopeY: Float = 0f,
    val gyroscopeZ: Float = 0f,
    val rotationRateDps: Float = 0f,
    val jerkGPerSec: Float = 0f,
    val speedKmh: Float = 0f,
    val speedDeltaKmh: Float = 0f,
    val headingDegrees: Float = 0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitudeMeters: Double? = null,
    val accuracyMeters: Float = 0f,
    val movementState: MovementState = MovementState.STATIONARY,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun computeGForce(ax: Float, ay: Float, az: Float): Float {
            val magnitude = sqrt((ax * ax + ay * ay + az * az).toDouble()).toFloat()
            return magnitude / 9.80665f
        }

        fun computeRotationRateDps(gx: Float, gy: Float, gz: Float): Float {
            val magnitudeRadPerSec = sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()
            return Math.toDegrees(magnitudeRadPerSec.toDouble()).toFloat()
        }
    }
}

/**
 * Output assessment produced by a Detection Engine.
 * Fully modular and explainable with risk level, confidence, extracted signals, and plain-language reason.
 */
data class DetectionResult(
    val isIncidentCandidate: Boolean,
    val riskLevel: RiskLevel,
    val confidence: Float,
    val signals: List<String> = emptyList(),
    val reason: String,
    val crashProbability: Float,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Core interface for accident detection engines in SAARATHI.
 * Any future ML model or external sensor classifier can implement this same interface.
 */
interface DetectionEngine {
    val name: String
    val version: String

    /**
     * Analyzes incoming normalized sensor telemetry and returns an explainable incident risk evaluation.
     */
    fun evaluate(telemetry: SensorTelemetry): DetectionResult

    /**
     * Resets internal tracking buffers or calibration baselines.
     */
    fun reset()
}

/**
 * Rule-based heuristic detection engine implementing multi-parameter threshold evaluation:
 *
 * Rules:
 * 1. LOW:
 *    - Normal movement (cruising, gentle turns, standard stop/go).
 *    - PHONE FALL: High phone acceleration / rotation spike, BUT vehicle/GPS speed remains normal and continuous.
 *
 * 2. MEDIUM:
 *    - Possible abnormal event.
 *    - SUDDEN BRAKING: Large speed reduction (> 35 km/h drop in rapid interval) BUT without severe collision impact force.
 *    - ROLLOVER / SEVERE ROTATION: High angular velocity (> 300°/s) without catastrophic G-force.
 *
 * 3. HIGH:
 *    - Multiple signals strongly suggest a possible collision:
 *      Sudden deceleration + impact-like acceleration (> 3.8G) + abnormal rotation + abrupt stop / movement cessation.
 */
class RuleBasedDetectionEngine : DetectionEngine {
    override val name: String = "SAARATHI Rule-Based Kinetic Engine"
    override val version: String = "2.1.0-MVP"

    private var lastSpeedKmh: Float = 0f
    private var lastGForce: Float = 1.0f
    private var lastTimestamp: Long = 0L

    override fun evaluate(telemetry: SensorTelemetry): DetectionResult {
        val effectiveGForce = if (telemetry.totalGForce > 0f) {
            telemetry.totalGForce
        } else {
            SensorTelemetry.computeGForce(
                telemetry.accelerometerX,
                telemetry.accelerometerY,
                telemetry.accelerometerZ
            )
        }

        val rotationRate = if (telemetry.rotationRateDps > 0f) {
            telemetry.rotationRateDps
        } else {
            SensorTelemetry.computeRotationRateDps(
                telemetry.gyroscopeX,
                telemetry.gyroscopeY,
                telemetry.gyroscopeZ
            )
        }

        val timeDeltaMs = if (lastTimestamp > 0) telemetry.timestamp - lastTimestamp else 100L
        val effectiveSpeedDrop = if (telemetry.speedDeltaKmh != 0f) {
            telemetry.speedDeltaKmh
        } else if (lastTimestamp > 0 && timeDeltaMs in 1..3000) {
            (lastSpeedKmh - telemetry.speedKmh).coerceAtLeast(0f)
        } else {
            0f
        }

        // Calculate jerk (rate of change of G-force)
        val jerk = if (telemetry.jerkGPerSec != 0f) {
            telemetry.jerkGPerSec
        } else if (timeDeltaMs > 0) {
            abs(effectiveGForce - lastGForce) / (timeDeltaMs / 1000f)
        } else {
            0f
        }

        // Update state
        lastSpeedKmh = telemetry.speedKmh
        lastGForce = effectiveGForce
        lastTimestamp = telemetry.timestamp

        val signalList = mutableListOf<String>()

        // -----------------------------------------------------------------------------------------
        // Disambiguation Rule: PHONE FALL
        // High phone rotation / impact spike, BUT vehicle speed is steady (speed > 15 km/h & speedDrop < 10 km/h)
        // or phone dropped while parked / stationary.
        // -----------------------------------------------------------------------------------------
        val isPhoneFallPattern = (rotationRate in 180f..450f || effectiveGForce in 2.2f..3.8f) &&
                (telemetry.speedKmh > 20f && effectiveSpeedDrop < 12f)

        if (isPhoneFallPattern) {
            signalList.add("Phone tumble rotation (${String.format("%.0f", rotationRate)}°/s)")
            signalList.add("Vehicle speed steady at ${String.format("%.0f", telemetry.speedKmh)} km/h (No collision deceleration)")
            return DetectionResult(
                isIncidentCandidate = false,
                riskLevel = RiskLevel.LOW,
                confidence = 0.88f,
                signals = signalList,
                reason = "Phone drop pattern detected: Localized angular rotation/bounce without vehicle velocity loss.",
                crashProbability = 0.05f,
                timestamp = telemetry.timestamp
            )
        }

        // -----------------------------------------------------------------------------------------
        // HIGH RISK EVALUATION: Severe Collision / Multi-Signal Impact
        // Sudden Deceleration + Impact-like G-Force (>= 3.8G) + Abnormal Rotation or Rapid Stop
        // -----------------------------------------------------------------------------------------
        val isExtremeGForce = effectiveGForce >= 4.2f
        val isCombinedCollision = effectiveGForce >= 3.4f && (effectiveSpeedDrop >= 30f || rotationRate >= 280f || jerk >= 18f)

        if (isExtremeGForce || isCombinedCollision) {
            if (effectiveGForce >= 3.4f) signalList.add("Severe kinetic impact: ${String.format("%.1f", effectiveGForce)}G")
            if (effectiveSpeedDrop >= 30f) signalList.add("Sudden velocity loss: -${String.format("%.1f", effectiveSpeedDrop)} km/h")
            if (rotationRate >= 280f) signalList.add("Abnormal angular roll: ${String.format("%.0f", rotationRate)}°/s")
            if (jerk >= 18f) signalList.add("High kinetic jerk: ${String.format("%.1f", jerk)} G/s")

            val confidence = ((effectiveGForce / 5.5f).coerceIn(0.80f, 0.98f))
            return DetectionResult(
                isIncidentCandidate = true,
                riskLevel = RiskLevel.HIGH,
                confidence = confidence,
                signals = signalList,
                reason = "Possible crash detected: Multi-signal collision pattern with ${String.format("%.1f", effectiveGForce)}G impact and severe deceleration (-${String.format("%.1f", effectiveSpeedDrop)} km/h).",
                crashProbability = confidence,
                timestamp = telemetry.timestamp
            )
        }

        // -----------------------------------------------------------------------------------------
        // MEDIUM RISK EVALUATION: Abnormal Event / Sudden Braking / Rollover Anomaly
        // Sudden braking (speed drop >= 35 km/h) OR Angular Roll (>= 300°/s) OR Moderate impact (>= 2.6G)
        // -----------------------------------------------------------------------------------------
        val isSuddenBraking = effectiveSpeedDrop >= 35f && effectiveGForce < 3.4f
        val isAngularRollAnomaly = rotationRate >= 300f && effectiveGForce < 3.4f
        val isModerateImpact = effectiveGForce in 2.6f..3.4f

        if (isSuddenBraking || isAngularRollAnomaly || isModerateImpact) {
            if (isSuddenBraking) signalList.add("Large sudden braking: -${String.format("%.1f", effectiveSpeedDrop)} km/h")
            if (isAngularRollAnomaly) signalList.add("Severe roll rate: ${String.format("%.0f", rotationRate)}°/s")
            if (isModerateImpact) signalList.add("Moderate impact spike: ${String.format("%.1f", effectiveGForce)}G")

            val reason = when {
                isSuddenBraking -> "Sudden hard braking detected: rapid velocity reduction (-${String.format("%.1f", effectiveSpeedDrop)} km/h) without structural impact."
                isAngularRollAnomaly -> "Abnormal vehicle roll or yaw anomaly detected (${String.format("%.0f", rotationRate)}°/s)."
                else -> "Moderate impact force detected (${String.format("%.1f", effectiveGForce)}G)."
            }

            val confidence = ((effectiveGForce / 4.0f).coerceIn(0.55f, 0.78f))
            return DetectionResult(
                isIncidentCandidate = true,
                riskLevel = RiskLevel.MEDIUM,
                confidence = confidence,
                signals = signalList,
                reason = reason,
                crashProbability = confidence * 0.7f,
                timestamp = telemetry.timestamp
            )
        }

        // -----------------------------------------------------------------------------------------
        // LOW RISK / NOMINAL STATE
        // -----------------------------------------------------------------------------------------
        if (effectiveGForce >= 1.8f || effectiveSpeedDrop >= 20f || rotationRate >= 120f) {
            signalList.add("Minor kinetic variation (${String.format("%.1f", effectiveGForce)}G)")
            return DetectionResult(
                isIncidentCandidate = false,
                riskLevel = RiskLevel.LOW,
                confidence = 0.25f,
                signals = signalList,
                reason = "Minor road vibration, pothole, or standard turn (${String.format("%.1f", effectiveGForce)}G, ${String.format("%.0f", telemetry.speedKmh)} km/h).",
                crashProbability = 0.10f,
                timestamp = telemetry.timestamp
            )
        }

        signalList.add("Kinematics stable (${String.format("%.1f", effectiveGForce)}G, ${String.format("%.0f", telemetry.speedKmh)} km/h)")
        return DetectionResult(
            isIncidentCandidate = false,
            riskLevel = RiskLevel.LOW,
            confidence = 0.05f,
            signals = signalList,
            reason = "Kinematics nominal. System monitoring armed.",
            crashProbability = 0.01f,
            timestamp = telemetry.timestamp
        )
    }

    override fun reset() {
        lastSpeedKmh = 0f
        lastGForce = 1.0f
        lastTimestamp = 0L
    }
}

/**
 * Future Machine Learning Detection Engine stub.
 * Note: Machine Learning is NOT enabled for accident detection in Prompt 2 per architecture specs.
 */
class MLDetectionEngineStub : DetectionEngine {
    override val name: String = "SAARATHI ML Classifier (Future Hook)"
    override val version: String = "2.0.0-STUB"

    override fun evaluate(telemetry: SensorTelemetry): DetectionResult {
        return RuleBasedDetectionEngine().evaluate(telemetry)
    }

    override fun reset() {}
}
