package com.example.data.repository

import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.IncidentDao
import com.example.data.local.dao.JourneyDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.UserSessionDao
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.IncidentEntity
import com.example.data.local.entities.JourneyEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.UserSessionEntity
import com.example.domain.model.EmergencyContact
import com.example.domain.model.Incident
import com.example.domain.model.IncidentStatus
import com.example.domain.model.Journey
import com.example.domain.model.JourneyStatus
import com.example.domain.model.UserProfile
import com.example.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepository(
    private val userSessionDao: UserSessionDao,
    private val profileDao: ProfileDao
) {
    val activeSession: Flow<UserSession?> = userSessionDao.getActiveSession().map { entity ->
        entity?.let {
            UserSession(
                userId = it.userId,
                email = it.email,
                fullName = it.fullName,
                token = it.token,
                expiresAt = it.expiresAt,
                isActive = it.isActive
            )
        }
    }

    suspend fun ensureDefaultSession(): UserSession {
        val existing = userSessionDao.getActiveSessionSync()
        if (existing != null) {
            return UserSession(
                userId = existing.userId,
                email = existing.email,
                fullName = existing.fullName,
                token = existing.token,
                expiresAt = existing.expiresAt,
                isActive = existing.isActive
            )
        }
        val defaultUserId = "usr-saarathi-primary"
        val now = System.currentTimeMillis()
        val defaultProfile = ProfileEntity(
            id = defaultUserId,
            fullName = "Arjun Kumar",
            email = "arjun.kumar@saarathi.org",
            phone = "+91 98765 43210",
            bloodType = "O+",
            medicalNotes = "Penicillin Allergy. Asthmatic. No cardiac history.",
            avatarUrl = null,
            createdAt = now,
            updatedAt = now
        )
        profileDao.upsertProfile(defaultProfile)

        val sessionEntity = UserSessionEntity(
            userId = defaultUserId,
            email = defaultProfile.email,
            fullName = defaultProfile.fullName,
            token = "sb_token_local_default",
            expiresAt = now + (365L * 24 * 60 * 60 * 1000),
            isActive = true,
            createdAt = now
        )
        userSessionDao.saveSession(sessionEntity)

        return UserSession(
            userId = sessionEntity.userId,
            email = sessionEntity.email,
            fullName = sessionEntity.fullName,
            token = sessionEntity.token,
            expiresAt = sessionEntity.expiresAt,
            isActive = true
        )
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        bloodType: String,
        medicalNotes: String,
        password: String
    ): Result<UserProfile> {
        val trimmedEmail = email.trim().lowercase()
        if (fullName.isBlank()) return Result.failure(IllegalArgumentException("Full name is required"))
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Valid email is required"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        val userId = UUID.randomUUID().toString()
        val token = "jwt_sb_" + UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        // 1. Create Profile
        val profileEntity = ProfileEntity(
            id = userId,
            fullName = fullName.trim(),
            email = trimmedEmail,
            phone = phone.trim(),
            bloodType = bloodType.trim().ifBlank { "O+" },
            medicalNotes = medicalNotes.trim(),
            avatarUrl = null,
            createdAt = now,
            updatedAt = now
        )
        profileDao.upsertProfile(profileEntity)

        // 2. Clear old sessions and persist new active session
        userSessionDao.clearActiveSessions()
        val sessionEntity = UserSessionEntity(
            userId = userId,
            email = trimmedEmail,
            fullName = fullName.trim(),
            token = token,
            expiresAt = now + (30L * 24 * 60 * 60 * 1000), // 30 days
            isActive = true,
            createdAt = now
        )
        userSessionDao.saveSession(sessionEntity)

        return Result.success(
            UserProfile(
                id = profileEntity.id,
                fullName = profileEntity.fullName,
                email = profileEntity.email,
                phone = profileEntity.phone,
                bloodType = profileEntity.bloodType,
                medicalNotes = profileEntity.medicalNotes,
                avatarUrl = profileEntity.avatarUrl,
                createdAt = profileEntity.createdAt,
                updatedAt = profileEntity.updatedAt
            )
        )
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Enter a valid email address"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }

        val now = System.currentTimeMillis()
        // If an existing profile matches email in local db, restore it.
        // Otherwise create demo session / login for user.
        val existingSession = userSessionDao.getActiveSessionSync()
        val userId = existingSession?.userId ?: UUID.randomUUID().toString()
        val existingProfile = profileDao.getProfileSync(userId)

        val profile = existingProfile ?: ProfileEntity(
            id = userId,
            fullName = "Saarathi Guardian",
            email = trimmedEmail,
            phone = "+91 98765 43210",
            bloodType = "B+",
            medicalNotes = "No major known drug allergies. NKDA.",
            avatarUrl = null,
            createdAt = now,
            updatedAt = now
        )
        profileDao.upsertProfile(profile)

        userSessionDao.clearActiveSessions()
        val sessionEntity = UserSessionEntity(
            userId = profile.id,
            email = profile.email.ifBlank { trimmedEmail },
            fullName = profile.fullName,
            token = "jwt_sb_" + UUID.randomUUID().toString(),
            expiresAt = now + (30L * 24 * 60 * 60 * 1000),
            isActive = true,
            createdAt = now
        )
        userSessionDao.saveSession(sessionEntity)

        return Result.success(
            UserProfile(
                id = profile.id,
                fullName = profile.fullName,
                email = profile.email,
                phone = profile.phone,
                bloodType = profile.bloodType,
                medicalNotes = profile.medicalNotes,
                avatarUrl = profile.avatarUrl,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt
            )
        )
    }

    suspend fun signOut() {
        userSessionDao.clearActiveSessions()
    }
}

class ProfileRepository(private val profileDao: ProfileDao) {
    fun getProfile(userId: String): Flow<UserProfile?> =
        profileDao.getProfile(userId).map { entity ->
            entity?.let {
                UserProfile(
                    id = it.id,
                    fullName = it.fullName,
                    email = it.email,
                    phone = it.phone,
                    bloodType = it.bloodType,
                    medicalNotes = it.medicalNotes,
                    avatarUrl = it.avatarUrl,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    suspend fun getProfileSync(userId: String): UserProfile? {
        val it = profileDao.getProfileSync(userId) ?: return null
        return UserProfile(
            id = it.id,
            fullName = it.fullName,
            email = it.email,
            phone = it.phone,
            bloodType = it.bloodType,
            medicalNotes = it.medicalNotes,
            avatarUrl = it.avatarUrl,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt
        )
    }

    suspend fun updateProfile(profile: UserProfile) {
        profileDao.upsertProfile(
            ProfileEntity(
                id = profile.id,
                fullName = profile.fullName,
                email = profile.email,
                phone = profile.phone,
                bloodType = profile.bloodType,
                medicalNotes = profile.medicalNotes,
                avatarUrl = profile.avatarUrl,
                createdAt = profile.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}

class EmergencyContactRepository(private val contactDao: EmergencyContactDao) {
    fun getContacts(userId: String): Flow<List<EmergencyContact>> =
        contactDao.getContactsForUser(userId).map { list ->
            list.map {
                EmergencyContact(
                    id = it.id,
                    userId = it.userId,
                    name = it.name,
                    phone = it.phone,
                    relationship = it.relationship,
                    priority = it.priority,
                    isActive = it.isActive,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    suspend fun upsertContact(contact: EmergencyContact) {
        contactDao.upsertContact(
            EmergencyContactEntity(
                id = contact.id.ifBlank { UUID.randomUUID().toString() },
                userId = contact.userId,
                name = contact.name,
                phone = contact.phone,
                relationship = contact.relationship,
                priority = contact.priority,
                isActive = contact.isActive,
                createdAt = contact.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteContact(contactId: String) {
        contactDao.deleteContactById(contactId)
    }
}

class JourneyRepository(private val journeyDao: JourneyDao) {
    fun getJourneys(userId: String): Flow<List<Journey>> =
        journeyDao.getJourneysForUser(userId).map { list ->
            list.map {
                Journey(
                    id = it.id,
                    userId = it.userId,
                    status = it.status,
                    startedAt = it.startedAt,
                    endedAt = it.endedAt,
                    startLatitude = it.startLatitude,
                    startLongitude = it.startLongitude,
                    endLatitude = it.endLatitude,
                    endLongitude = it.endLongitude,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    fun getActiveJourney(userId: String): Flow<Journey?> =
        journeyDao.getActiveJourney(userId).map { entity ->
            entity?.let {
                Journey(
                    id = it.id,
                    userId = it.userId,
                    status = it.status,
                    startedAt = it.startedAt,
                    endedAt = it.endedAt,
                    startLatitude = it.startLatitude,
                    startLongitude = it.startLongitude,
                    endLatitude = it.endLatitude,
                    endLongitude = it.endLongitude,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    suspend fun startJourney(
        userId: String,
        startLat: Double? = 12.9716,
        startLng: Double? = 77.5946
    ): Journey {
        val now = System.currentTimeMillis()
        val journey = JourneyEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            status = JourneyStatus.ACTIVE,
            startedAt = now,
            endedAt = null,
            startLatitude = startLat,
            startLongitude = startLng,
            endLatitude = null,
            endLongitude = null,
            createdAt = now,
            updatedAt = now
        )
        journeyDao.upsertJourney(journey)
        return Journey(
            id = journey.id,
            userId = journey.userId,
            status = journey.status,
            startedAt = journey.startedAt,
            startLatitude = journey.startLatitude,
            startLongitude = journey.startLongitude,
            createdAt = journey.createdAt,
            updatedAt = journey.updatedAt
        )
    }

    suspend fun endJourney(
        journeyId: String,
        endLat: Double? = 12.9750,
        endLng: Double? = 77.6000
    ) {
        val now = System.currentTimeMillis()
        journeyDao.updateJourneyStatus(
            journeyId = journeyId,
            status = JourneyStatus.COMPLETED,
            endedAt = now,
            updatedAt = now
        )
    }
}

class IncidentRepository(private val incidentDao: IncidentDao) {
    fun getIncidents(userId: String): Flow<List<Incident>> =
        incidentDao.getIncidentsForUser(userId).map { list ->
            list.map {
                Incident(
                    id = it.id,
                    userId = it.userId,
                    journeyId = it.journeyId,
                    status = it.status,
                    riskLevel = it.riskLevel,
                    crashProbability = it.crashProbability,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    detectedAt = it.detectedAt,
                    confirmedAt = it.confirmedAt,
                    resolvedAt = it.resolvedAt,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    notes = it.notes
                )
            }
        }

    suspend fun createIncident(incident: Incident): Incident {
        val entity = IncidentEntity(
            id = incident.id.ifBlank { UUID.randomUUID().toString() },
            userId = incident.userId,
            journeyId = incident.journeyId,
            status = incident.status,
            riskLevel = incident.riskLevel,
            crashProbability = incident.crashProbability,
            latitude = incident.latitude,
            longitude = incident.longitude,
            detectedAt = incident.detectedAt,
            confirmedAt = incident.confirmedAt,
            resolvedAt = incident.resolvedAt,
            createdAt = incident.createdAt,
            updatedAt = incident.updatedAt,
            notes = incident.notes
        )
        incidentDao.insertIncident(entity)
        return incident.copy(id = entity.id)
    }

    suspend fun updateIncident(incident: Incident) {
        incidentDao.updateIncident(
            IncidentEntity(
                id = incident.id,
                userId = incident.userId,
                journeyId = incident.journeyId,
                status = incident.status,
                riskLevel = incident.riskLevel,
                crashProbability = incident.crashProbability,
                latitude = incident.latitude,
                longitude = incident.longitude,
                detectedAt = incident.detectedAt,
                confirmedAt = incident.confirmedAt,
                resolvedAt = incident.resolvedAt,
                createdAt = incident.createdAt,
                updatedAt = System.currentTimeMillis(),
                notes = incident.notes
            )
        )
    }
}

class ResponderRepository(private val responderDao: com.example.data.local.dao.ResponderDao) {
    fun getResponderProfile(userId: String): Flow<com.example.domain.model.ResponderProfile?> =
        responderDao.getResponderProfile(userId).map { entity ->
            entity?.let {
                com.example.domain.model.ResponderProfile(
                    id = it.id,
                    userId = it.userId,
                    name = it.name,
                    phone = it.phone,
                    type = runCatching { com.example.domain.model.ResponderType.valueOf(it.type) }.getOrDefault(com.example.domain.model.ResponderType.VOLUNTEER),
                    verificationStatus = runCatching { com.example.domain.model.VerificationStatus.valueOf(it.verificationStatus) }.getOrDefault(com.example.domain.model.VerificationStatus.UNVERIFIED),
                    languages = it.languages.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() },
                    isAvailable = it.isAvailable,
                    trainingDetails = it.trainingDetails,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    address = it.address,
                    rating = it.rating,
                    totalResponses = it.totalResponses,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        }

    suspend fun upsertResponderProfile(profile: com.example.domain.model.ResponderProfile) {
        responderDao.upsertResponderProfile(
            com.example.data.local.entities.ResponderProfileEntity(
                id = profile.id,
                userId = profile.userId,
                name = profile.name,
                phone = profile.phone,
                type = profile.type.name,
                verificationStatus = profile.verificationStatus.name,
                languages = profile.languages.joinToString(", "),
                isAvailable = profile.isAvailable,
                trainingDetails = profile.trainingDetails,
                latitude = profile.latitude,
                longitude = profile.longitude,
                address = profile.address,
                rating = profile.rating,
                totalResponses = profile.totalResponses,
                createdAt = profile.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleAvailability(userId: String, isAvailable: Boolean) {
        responderDao.updateAvailability(userId, isAvailable)
    }

    suspend fun saveAssignment(assignment: com.example.domain.model.ResponderAssignment) {
        responderDao.insertAssignment(
            com.example.data.local.entities.ResponderAssignmentEntity(
                id = assignment.id,
                incidentId = assignment.incidentId,
                responderId = assignment.responderId,
                responderName = assignment.responderName,
                responderPhone = assignment.responderPhone,
                responderType = assignment.responderType.name,
                status = assignment.status.name,
                distanceMeters = assignment.distanceMeters,
                approximateLocation = assignment.approximateLocation,
                incidentRisk = assignment.incidentRisk.name,
                assignedAt = assignment.assignedAt,
                respondedAt = assignment.respondedAt,
                etaMinutes = assignment.etaMinutes,
                notes = assignment.notes
            )
        )
    }

    suspend fun updateAssignmentStatus(assignmentId: String, status: com.example.domain.model.AssignmentStatus) {
        responderDao.updateAssignmentStatus(assignmentId, status.name, System.currentTimeMillis())
    }
}

class OfflineSyncRepository(private val pendingSyncDao: com.example.data.local.dao.PendingSyncDao) {
    suspend fun queueIncidentSync(incident: Incident) {
        pendingSyncDao.queueSyncItem(
            com.example.data.local.entities.PendingSyncEntity(
                id = java.util.UUID.randomUUID().toString(),
                tableName = "incidents",
                operation = "INSERT",
                payloadJson = "{\"id\":\"${incident.id}\",\"risk\":\"${incident.riskLevel}\",\"lat\":${incident.latitude},\"lng\":${incident.longitude}}",
                status = "PENDING_SYNC",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getPendingSyncCount(): Int {
        return pendingSyncDao.getPendingSyncItems().size
    }
}
