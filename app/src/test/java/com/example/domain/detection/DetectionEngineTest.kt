package com.example.domain.detection

import com.example.domain.model.MovementState
import com.example.domain.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DetectionEngineTest {

    private lateinit var engine: RuleBasedDetectionEngine

    @Before
    fun setUp() {
        engine = RuleBasedDetectionEngine()
    }

    @Test
    fun `test normal steady cruising returns LOW risk non-candidate nominal result`() {
        val telemetry = SensorTelemetry(
            accelerometerX = 0.2f,
            accelerometerY = 0.1f,
            accelerometerZ = 9.8f,
            totalGForce = 1.0f,
            gyroscopeX = 0.01f,
            gyroscopeY = 0.02f,
            gyroscopeZ = 0.01f,
            rotationRateDps = 5f,
            speedKmh = 60f,
            movementState = MovementState.CRUISING,
            timestamp = 1000L
        )

        val result = engine.evaluate(telemetry)

        assertFalse("Normal driving must not trigger incident candidate", result.isIncidentCandidate)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertTrue(result.crashProbability < 0.1f)
        assertTrue(result.signals.isNotEmpty())
    }

    @Test
    fun `test phone-like movement or drop inside moving vehicle is disambiguated to LOW risk`() {
        // Phone dropped in car: rotation spike (320°/s) and bounce (2.8G), but vehicle cruising at 55 km/h with no speed drop
        val telemetry = SensorTelemetry(
            totalGForce = 2.8f,
            rotationRateDps = 320f,
            speedKmh = 55f,
            speedDeltaKmh = 2f,
            movementState = MovementState.CRUISING,
            timestamp = 1000L
        )

        val result = engine.evaluate(telemetry)

        assertFalse("Phone fall inside cruising vehicle must not trigger incident", result.isIncidentCandidate)
        assertEquals(RiskLevel.LOW, result.riskLevel)
        assertTrue(result.reason.contains("Phone drop pattern detected"))
        assertTrue(result.signals.any { it.contains("Phone tumble") || it.contains("speed steady") })
    }

    @Test
    fun `test sudden braking with large speed reduction returns MEDIUM risk`() {
        // Sudden braking: speed dropped from 60 to 12 km/h (-48 km/h drop), but moderate G-force (< 3.0G)
        val telemetry = SensorTelemetry(
            totalGForce = 1.9f,
            rotationRateDps = 25f,
            speedKmh = 12f,
            speedDeltaKmh = 48f,
            movementState = MovementState.HARD_BRAKING,
            timestamp = 1000L
        )

        val result = engine.evaluate(telemetry)

        assertTrue("Sudden braking should flag as candidate for non-intrusive monitoring", result.isIncidentCandidate)
        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertTrue(result.reason.contains("Sudden hard braking detected"))
    }

    @Test
    fun `test sudden rotation or rollover anomaly returns MEDIUM risk`() {
        // Rollover anomaly: extreme angular rate (360°/s)
        val telemetry = SensorTelemetry(
            totalGForce = 2.7f,
            rotationRateDps = 360f,
            speedKmh = 25f,
            speedDeltaKmh = 20f,
            movementState = MovementState.TURNING,
            timestamp = 1000L
        )

        val result = engine.evaluate(telemetry)

        assertTrue(result.isIncidentCandidate)
        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertTrue(result.reason.contains("roll or yaw anomaly"))
    }

    @Test
    fun `test combined high-risk collision crash returns HIGH risk candidate with explainable signals`() {
        // High-G collision: 4.9G impact, speed drop 65 km/h to 0 km/h, angular roll 420°/s
        val telemetry = SensorTelemetry(
            accelerometerX = 28.0f,
            accelerometerY = -38.0f,
            accelerometerZ = 20.0f,
            totalGForce = 4.9f,
            rotationRateDps = 420f,
            jerkGPerSec = 25.0f,
            speedKmh = 0f,
            speedDeltaKmh = 65f,
            movementState = MovementState.ABNORMAL_IMPACT,
            timestamp = 1000L
        )

        val result = engine.evaluate(telemetry)

        assertTrue("High G crash must be marked as candidate", result.isIncidentCandidate)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(result.confidence >= 0.80f)
        assertTrue(result.reason.contains("Possible crash detected"))
        assertTrue(result.signals.any { it.contains("Severe kinetic impact") })
        assertTrue(result.signals.any { it.contains("Sudden velocity loss") })
    }

    @Test
    fun `test computeGForce and computeRotationRateDps formulas`() {
        val gAtRest = SensorTelemetry.computeGForce(0f, 0f, 9.80665f)
        assertEquals(1.0f, gAtRest, 0.05f)

        val rotDps = SensorTelemetry.computeRotationRateDps(0f, 0f, Math.PI.toFloat())
        assertEquals(180f, rotDps, 1.0f)
    }
}
