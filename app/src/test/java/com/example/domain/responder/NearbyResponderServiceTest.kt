package com.example.domain.responder

import com.example.domain.model.Incident
import com.example.domain.model.IncidentStatus
import com.example.domain.model.ResponderProfile
import com.example.domain.model.ResponderType
import com.example.domain.model.RiskLevel
import com.example.domain.model.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NearbyResponderServiceTest {

    @Before
    fun setUp() {
        NearbyResponderService.registerOrUpdateResponder(
            ResponderProfile(
                id = "resp-1",
                userId = "user-1",
                name = "Dr. Priya Rao",
                phone = "+919876543210",
                type = ResponderType.MEDICAL_PROFESSIONAL,
                languages = listOf("English", "Hindi"),
                isAvailable = true,
                verificationStatus = VerificationStatus.CREDENTIAL_VERIFIED,
                address = "MG Road, Bengaluru",
                trainingDetails = "Apollo Emergency Medicine Fellow",
                createdAt = System.currentTimeMillis(),
                latitude = 12.9720, // ~100m away from 12.9716, 77.5946
                longitude = 77.5950
            )
        )

        NearbyResponderService.registerOrUpdateResponder(
            ResponderProfile(
                id = "resp-2",
                userId = "user-2",
                name = "Rahul Sharma",
                phone = "+919876543211",
                type = ResponderType.CPR_TRAINED,
                languages = listOf("English"),
                isAvailable = true,
                verificationStatus = VerificationStatus.COMMUNITY_VERIFIED,
                address = "Indiranagar, Bengaluru",
                trainingDetails = "Red Cross Certified",
                createdAt = System.currentTimeMillis(),
                latitude = 12.9780, // ~1.5km away
                longitude = 77.6400
            )
        )

        NearbyResponderService.registerOrUpdateResponder(
            ResponderProfile(
                id = "resp-3",
                userId = "user-3",
                name = "Ananya Desai",
                phone = "+919876543212",
                type = ResponderType.VOLUNTEER,
                languages = emptyList(),
                isAvailable = false, // Offline
                verificationStatus = VerificationStatus.UNVERIFIED,
                address = "Koramangala, Bengaluru",
                trainingDetails = "",
                createdAt = System.currentTimeMillis(),
                latitude = 12.9716,
                longitude = 77.5946
            )
        )
    }

    @Test
    fun testHaversineDistanceCalculation() {
        val dist = NearbyResponderService.calculateHaversineDistance(
            12.9716, 77.5946,
            12.9720, 77.5950
        )
        assertTrue("Distance should be under 200m", dist in 1.0..200.0)
    }

    @Test
    fun testFindNearbyRespondersFiltersOfflineAndRadius() {
        val results = NearbyResponderService.findNearbyResponders(
            incidentLat = 12.9716,
            incidentLng = 77.5946,
            maxRadiusMeters = 3000.0,
            incidentRisk = RiskLevel.HIGH
        )

        // resp-3 is offline -> should be excluded
        // resp-1 and resp-2 are within 3km and active
        assertTrue("Should return available responders within radius", results.isNotEmpty())
        assertEquals("Closest available responder should be resp-1", "resp-1", results.first().first.id)
    }

    @Test
    fun testCreateAssignmentRequest() {
        val incident = Incident(
            id = "inc-test-123",
            userId = "victim-user-1",
            journeyId = "journey-1",
            status = IncidentStatus.CONFIRMED,
            riskLevel = RiskLevel.HIGH,
            crashProbability = 0.95f,
            latitude = 12.9716,
            longitude = 77.5946,
            detectedAt = System.currentTimeMillis(),
            notes = "High-G frontal impact"
        )

        val responder = NearbyResponderService.getResponderByUserId("user-1")
        assertNotNull(responder)

        val request = NearbyResponderService.createAssignmentRequest(
            incident = incident,
            responder = responder!!,
            distanceMeters = 350.0
        )

        assertEquals("inc-test-123", request.incidentId)
        assertEquals("resp-1", request.responderId)
        assertTrue("ETA should be calculated realistically", request.etaMinutes >= 1)
        assertEquals(350.0, request.distanceMeters, 0.1)
    }
}
