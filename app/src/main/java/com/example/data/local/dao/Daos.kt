package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.IncidentEntity
import com.example.data.local.entities.JourneyEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.UserSessionEntity
import com.example.domain.model.JourneyStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :userId LIMIT 1")
    fun getProfile(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :userId LIMIT 1")
    suspend fun getProfileSync(userId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :userId")
    suspend fun deleteProfile(userId: String)
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId ORDER BY priority ASC, createdAt ASC")
    fun getContactsForUser(userId: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId AND isActive = 1 ORDER BY priority ASC")
    suspend fun getActiveContactsSync(userId: String): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): EmergencyContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertContact(contact: EmergencyContactEntity)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: String)
}

@Dao
interface JourneyDao {
    @Query("SELECT * FROM journeys WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJourneysForUser(userId: String): Flow<List<JourneyEntity>>

    @Query("SELECT * FROM journeys WHERE userId = :userId AND status = :status ORDER BY createdAt DESC LIMIT 1")
    fun getActiveJourney(userId: String, status: JourneyStatus = JourneyStatus.ACTIVE): Flow<JourneyEntity?>

    @Query("SELECT * FROM journeys WHERE userId = :userId AND status = :status ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveJourneySync(userId: String, status: JourneyStatus = JourneyStatus.ACTIVE): JourneyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJourney(journey: JourneyEntity)

    @Query("UPDATE journeys SET status = :status, endedAt = :endedAt, updatedAt = :updatedAt WHERE id = :journeyId")
    suspend fun updateJourneyStatus(journeyId: String, status: JourneyStatus, endedAt: Long?, updatedAt: Long)

    @Query("DELETE FROM journeys WHERE id = :id")
    suspend fun deleteJourney(id: String)
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents WHERE userId = :userId ORDER BY detectedAt DESC")
    fun getIncidentsForUser(userId: String): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id LIMIT 1")
    fun getIncidentById(id: String): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents WHERE id = :id LIMIT 1")
    suspend fun getIncidentByIdSync(id: String): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncident(id: String)
}

@Dao
interface UserSessionDao {
    @Query("SELECT * FROM user_sessions WHERE isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    fun getActiveSession(): Flow<UserSessionEntity?>

    @Query("SELECT * FROM user_sessions WHERE isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveSessionSync(): UserSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)

    @Query("UPDATE user_sessions SET isActive = 0")
    suspend fun clearActiveSessions()

    @Query("DELETE FROM user_sessions")
    suspend fun deleteAllSessions()
}

@Dao
interface ResponderDao {
    @Query("SELECT * FROM responder_profiles WHERE userId = :userId LIMIT 1")
    fun getResponderProfile(userId: String): Flow<com.example.data.local.entities.ResponderProfileEntity?>

    @Query("SELECT * FROM responder_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getResponderProfileSync(userId: String): com.example.data.local.entities.ResponderProfileEntity?

    @Query("SELECT * FROM responder_profiles WHERE isAvailable = 1")
    suspend fun getAvailableRespondersSync(): List<com.example.data.local.entities.ResponderProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResponderProfile(profile: com.example.data.local.entities.ResponderProfileEntity)

    @Query("UPDATE responder_profiles SET isAvailable = :isAvailable WHERE userId = :userId")
    suspend fun updateAvailability(userId: String, isAvailable: Boolean)

    // Assignments
    @Query("SELECT * FROM responder_assignments WHERE incidentId = :incidentId ORDER BY assignedAt DESC")
    fun getAssignmentsForIncident(incidentId: String): Flow<List<com.example.data.local.entities.ResponderAssignmentEntity>>

    @Query("SELECT * FROM responder_assignments WHERE responderId = :responderId ORDER BY assignedAt DESC")
    fun getAssignmentsForResponder(responderId: String): Flow<List<com.example.data.local.entities.ResponderAssignmentEntity>>

    @Query("SELECT * FROM responder_assignments WHERE id = :assignmentId LIMIT 1")
    suspend fun getAssignmentById(assignmentId: String): com.example.data.local.entities.ResponderAssignmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: com.example.data.local.entities.ResponderAssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: com.example.data.local.entities.ResponderAssignmentEntity)

    @Query("UPDATE responder_assignments SET status = :status, respondedAt = :respondedAt WHERE id = :assignmentId")
    suspend fun updateAssignmentStatus(assignmentId: String, status: String, respondedAt: Long?)
}

@Dao
interface PendingSyncDao {
    @Query("SELECT * FROM pending_sync_queue WHERE status = 'PENDING_SYNC' ORDER BY timestamp ASC")
    suspend fun getPendingSyncItems(): List<com.example.data.local.entities.PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun queueSyncItem(item: com.example.data.local.entities.PendingSyncEntity)

    @Query("UPDATE pending_sync_queue SET status = :status, attempts = attempts + 1 WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("DELETE FROM pending_sync_queue WHERE id = :id")
    suspend fun deleteSyncItem(id: String)
}
