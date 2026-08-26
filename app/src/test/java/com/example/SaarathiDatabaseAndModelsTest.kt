package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SaarathiDatabase
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.IncidentEntity
import com.example.data.local.entities.JourneyEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.UserSessionEntity
import com.example.domain.model.IncidentStatus
import com.example.domain.model.JourneyStatus
import com.example.domain.model.RiskLevel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SaarathiDatabaseAndModelsTest {

    private lateinit var db: SaarathiDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SaarathiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `test profile insertion and retrieval`() = runBlocking {
        val profile = ProfileEntity(
            id = "user_test_123",
            fullName = "Ananya Roy",
            email = "ananya@saarathi.org",
            phone = "+91 98765 00001",
            bloodType = "O+",
            medicalNotes = "Penicillin allergy"
        )
        db.profileDao().upsertProfile(profile)

        val retrieved = db.profileDao().getProfileSync("user_test_123")
        assertNotNull(retrieved)
        assertEquals("Ananya Roy", retrieved?.fullName)
        assertEquals("O+", retrieved?.bloodType)
        assertEquals("Penicillin allergy", retrieved?.medicalNotes)
    }

    @Test
    fun `test emergency contacts priority and retrieval`() = runBlocking {
        val userId = "user_test_123"
        val contact1 = EmergencyContactEntity(
            id = "c1",
            userId = userId,
            name = "Ravi Roy",
            phone = "+91 98450 11111",
            relationship = "Father",
            priority = 2
        )
        val contact2 = EmergencyContactEntity(
            id = "c2",
            userId = userId,
            name = "Meera Roy",
            phone = "+91 98450 22222",
            relationship = "Mother",
            priority = 1
        )
        db.emergencyContactDao().upsertContact(contact1)
        db.emergencyContactDao().upsertContact(contact2)

        val activeList = db.emergencyContactDao().getActiveContactsSync(userId)
        assertEquals(2, activeList.size)
        // Priority 1 should come before Priority 2
        assertEquals("c2", activeList[0].id)
        assertEquals("c1", activeList[1].id)
    }

    @Test
    fun `test journey lifecycle states`() = runBlocking {
        val userId = "user_test_123"
        val journeyId = "j_001"
        val startJourney = JourneyEntity(
            id = journeyId,
            userId = userId,
            status = JourneyStatus.ACTIVE,
            startedAt = 10000L,
            startLatitude = 12.9716,
            startLongitude = 77.5946
        )
        db.journeyDao().upsertJourney(startJourney)

        val active = db.journeyDao().getActiveJourneySync(userId)
        assertNotNull(active)
        assertEquals(JourneyStatus.ACTIVE, active?.status)

        // End Journey
        db.journeyDao().updateJourneyStatus(journeyId, JourneyStatus.COMPLETED, 25000L, 25000L)
        val activeAfterEnd = db.journeyDao().getActiveJourneySync(userId)
        assertNull(activeAfterEnd)
    }

    @Test
    fun `test incident creation and update to emergency confirmed`() = runBlocking {
        val incident = IncidentEntity(
            id = "inc_999",
            userId = "user_test_123",
            status = IncidentStatus.AWAITING_CONFIRMATION,
            riskLevel = RiskLevel.HIGH,
            crashProbability = 0.95f,
            latitude = 12.9716,
            longitude = 77.5946,
            notes = "High kinetic impact detected"
        )
        db.incidentDao().insertIncident(incident)

        val retrieved = db.incidentDao().getIncidentByIdSync("inc_999")
        assertNotNull(retrieved)
        assertEquals(IncidentStatus.AWAITING_CONFIRMATION, retrieved?.status)

        // Escalate to confirmed
        val confirmed = retrieved!!.copy(
            status = IncidentStatus.CONFIRMED,
            confirmedAt = System.currentTimeMillis()
        )
        db.incidentDao().updateIncident(confirmed)

        val updated = db.incidentDao().getIncidentByIdSync("inc_999")
        assertEquals(IncidentStatus.CONFIRMED, updated?.status)
    }

    @Test
    fun `test user session persistence and clearing`() = runBlocking {
        val session = UserSessionEntity(
            userId = "user_test_123",
            email = "guardian@saarathi.org",
            fullName = "Ananya Roy",
            token = "jwt_test_token_abc",
            expiresAt = System.currentTimeMillis() + 100000L,
            isActive = true
        )
        db.userSessionDao().saveSession(session)

        val activeSession = db.userSessionDao().getActiveSessionSync()
        assertNotNull(activeSession)
        assertEquals("user_test_123", activeSession?.userId)

        db.userSessionDao().clearActiveSessions()
        val clearedSession = db.userSessionDao().getActiveSessionSync()
        assertNull(clearedSession)
    }
}
