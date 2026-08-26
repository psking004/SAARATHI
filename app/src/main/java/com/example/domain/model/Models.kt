package com.example.domain.model

enum class JourneyStatus {
    IDLE,
    ACTIVE,
    COMPLETED,
    CANCELLED
}

enum class IncidentStatus {
    DETECTED,
    AWAITING_CONFIRMATION,
    CONFIRMED,
    ESCALATED,
    RESOLVED,
    CANCELLED
}

/**
 * Complete Emergency State Machine States for Golden Hour Workflow.
 * NORMAL -> POSSIBLE_INCIDENT -> CONFIRMATION_REQUIRED -> EMERGENCY_ACTIVE ->
 * HELP_REQUESTED -> RESPONDER_FOUND -> PROFESSIONAL_HELP_PENDING -> RESOLVED (or CANCELLED)
 */
enum class EmergencyState {
    NORMAL,
    POSSIBLE_INCIDENT,
    CONFIRMATION_REQUIRED,
    EMERGENCY_ACTIVE,
    HELP_REQUESTED,
    RESPONDER_FOUND,
    PROFESSIONAL_HELP_PENDING,
    RESOLVED,
    CANCELLED;

    fun canTransitionTo(next: EmergencyState): Boolean {
        return when (this) {
            NORMAL -> next in listOf(POSSIBLE_INCIDENT, CONFIRMATION_REQUIRED, EMERGENCY_ACTIVE)
            POSSIBLE_INCIDENT -> next in listOf(NORMAL, CONFIRMATION_REQUIRED, EMERGENCY_ACTIVE, CANCELLED)
            CONFIRMATION_REQUIRED -> next in listOf(EMERGENCY_ACTIVE, CANCELLED, NORMAL)
            EMERGENCY_ACTIVE -> next in listOf(HELP_REQUESTED, RESPONDER_FOUND, PROFESSIONAL_HELP_PENDING, RESOLVED, CANCELLED)
            HELP_REQUESTED -> next in listOf(RESPONDER_FOUND, PROFESSIONAL_HELP_PENDING, RESOLVED, CANCELLED)
            RESPONDER_FOUND -> next in listOf(PROFESSIONAL_HELP_PENDING, RESOLVED, CANCELLED)
            PROFESSIONAL_HELP_PENDING -> next in listOf(RESOLVED, CANCELLED)
            RESOLVED -> next == NORMAL
            CANCELLED -> next == NORMAL
        }
    }
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

enum class MovementState {
    STATIONARY,
    CRUISING,
    DECELERATING,
    HARD_BRAKING,
    TURNING,
    ABNORMAL_IMPACT
}

enum class ResponderType {
    VOLUNTEER,
    FIRST_AID_TRAINED,
    CPR_TRAINED,
    MEDICAL_PROFESSIONAL
}

enum class VerificationStatus {
    UNVERIFIED,
    COMMUNITY_VERIFIED,
    CREDENTIAL_VERIFIED
}

enum class AssignmentStatus {
    REQUESTED,
    ACCEPTED,
    DECLINED,
    EN_ROUTE,
    ON_SCENE,
    COMPLETED,
    CANCELLED
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    SYNC_FAILED
}

enum class RegionalLanguage(val displayName: String, val nativeName: String, val code: String) {
    ENGLISH("English", "English", "en"),
    HINDI("Hindi", "हिन्दी", "hi"),
    KANNADA("Kannada", "ಕನ್ನಡ", "kn"),
    TAMIL("Tamil", "தமிழ்", "ta")
}

data class ResponderProfile(
    val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val type: ResponderType = ResponderType.VOLUNTEER,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val languages: List<String> = listOf("English"),
    val isAvailable: Boolean = true,
    val trainingDetails: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "",
    val rating: Float = 5.0f,
    val totalResponses: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ResponderAssignment(
    val id: String,
    val incidentId: String,
    val responderId: String,
    val responderName: String,
    val responderPhone: String,
    val responderType: ResponderType,
    val status: AssignmentStatus = AssignmentStatus.REQUESTED,
    val distanceMeters: Double = 0.0,
    val approximateLocation: String = "",
    val incidentRisk: RiskLevel = RiskLevel.HIGH,
    val assignedAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val etaMinutes: Int = 5,
    val notes: String = ""
)

data class ResponderLocation(
    val responderId: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: Long = System.currentTimeMillis()
)

data class NotificationDeliveryResult(
    val isDelivered: Boolean,
    val channel: String,
    val providerName: String,
    val statusMessage: String,
    val recipientCount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String = "",
    val phone: String = "",
    val bloodType: String = "",
    val medicalNotes: String = "",
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class EmergencyContact(
    val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Float = 0f,
    val headingDegrees: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class Journey(
    val id: String,
    val userId: String,
    val status: JourneyStatus = JourneyStatus.IDLE,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val sampledWaypoints: List<LocationPoint> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Incident(
    val id: String,
    val userId: String,
    val journeyId: String? = null,
    val status: IncidentStatus = IncidentStatus.DETECTED,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val crashProbability: Float = 0.0f,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val detectedAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class UserSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val token: String,
    val expiresAt: Long,
    val isActive: Boolean = true
)
