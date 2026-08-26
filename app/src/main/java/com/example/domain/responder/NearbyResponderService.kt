package com.example.domain.responder

import com.example.domain.model.AssignmentStatus
import com.example.domain.model.Incident
import com.example.domain.model.ResponderAssignment
import com.example.domain.model.ResponderProfile
import com.example.domain.model.ResponderType
import com.example.domain.model.RiskLevel
import com.example.domain.model.VerificationStatus
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object NearbyResponderService {

    // Verified / Community sample responder pool for demonstration & local matching
    private val communityResponders = mutableListOf(
        ResponderProfile(
            id = "resp-001",
            userId = "user-resp-001",
            name = "Dr. Anita Sharma",
            phone = "+91 98450 11223",
            type = ResponderType.MEDICAL_PROFESSIONAL,
            verificationStatus = VerificationStatus.CREDENTIAL_VERIFIED,
            languages = listOf("English", "Hindi", "Kannada"),
            isAvailable = true,
            trainingDetails = "MBBS, MD Critical Care - Manipal Hospital",
            latitude = 12.9725,
            longitude = 77.5955,
            address = "Near MG Road, Bangalore",
            rating = 4.9f,
            totalResponses = 14
        ),
        ResponderProfile(
            id = "resp-002",
            userId = "user-resp-002",
            name = "Rajesh Gowda",
            phone = "+91 99001 22334",
            type = ResponderType.CPR_TRAINED,
            verificationStatus = VerificationStatus.COMMUNITY_VERIFIED,
            languages = listOf("Kannada", "English"),
            isAvailable = true,
            trainingDetails = "St. John Ambulance Certified First Responder (CPR/AED)",
            latitude = 12.9738,
            longitude = 77.5970,
            address = "Near Brigade Road, Bangalore",
            rating = 4.8f,
            totalResponses = 8
        ),
        ResponderProfile(
            id = "resp-003",
            userId = "user-resp-003",
            name = "Vikram Patel",
            phone = "+91 97400 33445",
            type = ResponderType.FIRST_AID_TRAINED,
            verificationStatus = VerificationStatus.COMMUNITY_VERIFIED,
            languages = listOf("Hindi", "English"),
            isAvailable = true,
            trainingDetails = "Red Cross Basic Life Support",
            latitude = 12.9700,
            longitude = 77.5920,
            address = "Near Richmond Town, Bangalore",
            rating = 4.7f,
            totalResponses = 5
        ),
        ResponderProfile(
            id = "resp-004",
            userId = "user-resp-004",
            name = "Kavita Rao",
            phone = "+91 94480 55667",
            type = ResponderType.VOLUNTEER,
            verificationStatus = VerificationStatus.UNVERIFIED,
            languages = listOf("English", "Tamil", "Kannada"),
            isAvailable = true,
            trainingDetails = "Community Safety Volunteer",
            latitude = 12.9760,
            longitude = 77.5980,
            address = "Near Residency Road, Bangalore",
            rating = 5.0f,
            totalResponses = 2
        )
    )

    fun registerOrUpdateResponder(profile: ResponderProfile) {
        val index = communityResponders.indexOfFirst { it.id == profile.id || it.userId == profile.userId }
        if (index >= 0) {
            communityResponders[index] = profile
        } else {
            communityResponders.add(profile)
        }
    }

    fun getAllResponders(): List<ResponderProfile> = communityResponders.toList()

    fun getResponderByUserId(userId: String): ResponderProfile? {
        return communityResponders.firstOrNull { it.userId == userId }
    }

    /**
     * Finds nearby available responders within [maxRadiusMeters] (default 5000m / 5km).
     * Ranks by:
     * 1. Qualification level (MEDICAL_PROFESSIONAL > CPR_TRAINED > FIRST_AID_TRAINED > VOLUNTEER)
     * 2. Distance to incident.
     */
    fun findNearbyResponders(
        incidentLat: Double,
        incidentLng: Double,
        maxRadiusMeters: Double = 5000.0,
        incidentRisk: RiskLevel = RiskLevel.HIGH
    ): List<Pair<ResponderProfile, Double>> {
        return communityResponders
            .filter { it.isAvailable && it.latitude != null && it.longitude != null }
            .map { responder ->
                val distance = calculateHaversineDistance(
                    lat1 = incidentLat,
                    lon1 = incidentLng,
                    lat2 = responder.latitude!!,
                    lon2 = responder.longitude!!
                )
                Pair(responder, distance)
            }
            .filter { it.second <= maxRadiusMeters }
            .sortedWith(
                compareBy<Pair<ResponderProfile, Double>> {
                    when (it.first.type) {
                        ResponderType.MEDICAL_PROFESSIONAL -> 0
                        ResponderType.CPR_TRAINED -> 1
                        ResponderType.FIRST_AID_TRAINED -> 2
                        ResponderType.VOLUNTEER -> 3
                    }
                }.thenBy { it.second }
            )
    }

    /**
     * Generates a responder assignment request for the best candidate.
     * Note: Professional 112/108 response always has priority.
     */
    fun createAssignmentRequest(
        incident: Incident,
        responder: ResponderProfile,
        distanceMeters: Double
    ): ResponderAssignment {
        val approximateArea = "Near Outer Ring Road / Central Corridor (~${String.format("%.1f", distanceMeters / 1000)} km)"
        val etaMinutes = ((distanceMeters / 400.0) + 1.0).toInt().coerceAtLeast(2)

        return ResponderAssignment(
            id = "assign-" + UUID.randomUUID().toString().take(8),
            incidentId = incident.id,
            responderId = responder.id,
            responderName = responder.name,
            responderPhone = responder.phone,
            responderType = responder.type,
            status = AssignmentStatus.REQUESTED,
            distanceMeters = distanceMeters,
            approximateLocation = approximateArea,
            incidentRisk = incident.riskLevel,
            assignedAt = System.currentTimeMillis(),
            etaMinutes = etaMinutes,
            notes = "Emergency request dispatched. Follow emergency-service instructions."
        )
    }

    /**
     * Haversine formula to compute great-circle distance in meters between two lat/lng points.
     */
    fun calculateHaversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }
}
