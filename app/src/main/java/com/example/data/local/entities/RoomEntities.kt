package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.IncidentStatus
import com.example.domain.model.JourneyStatus
import com.example.domain.model.RiskLevel

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String = "",
    val phone: String = "",
    val bloodType: String = "",
    val medicalNotes: String = "",
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "emergency_contacts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "priority"])
    ]
)
data class EmergencyContactEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val relationship: String,
    val priority: Int = 1,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "journeys",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"])
    ]
)
data class JourneyEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val status: JourneyStatus = JourneyStatus.IDLE,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "incidents",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["journeyId"]),
        Index(value = ["status"])
    ]
)
data class IncidentEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val fullName: String,
    val token: String,
    val expiresAt: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "responder_profiles",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["isAvailable"]),
        Index(value = ["type"])
    ]
)
data class ResponderProfileEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val phone: String,
    val type: String, // ResponderType
    val verificationStatus: String, // VerificationStatus
    val languages: String = "English",
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

@Entity(
    tableName = "responder_assignments",
    indices = [
        Index(value = ["incidentId"]),
        Index(value = ["responderId"]),
        Index(value = ["status"])
    ]
)
data class ResponderAssignmentEntity(
    @PrimaryKey val id: String,
    val incidentId: String,
    val responderId: String,
    val responderName: String,
    val responderPhone: String,
    val responderType: String,
    val status: String,
    val distanceMeters: Double = 0.0,
    val approximateLocation: String = "",
    val incidentRisk: String = "HIGH",
    val assignedAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val etaMinutes: Int = 5,
    val notes: String = ""
)

@Entity(
    tableName = "pending_sync_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["timestamp"])
    ]
)
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val tableName: String,
    val operation: String, // INSERT, UPDATE, DELETE
    val payloadJson: String,
    val status: String = "PENDING_SYNC",
    val attempts: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
