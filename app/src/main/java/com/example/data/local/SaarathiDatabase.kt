package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.IncidentDao
import com.example.data.local.dao.JourneyDao
import com.example.data.local.dao.PendingSyncDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.dao.ResponderDao
import com.example.data.local.dao.UserSessionDao
import com.example.data.local.entities.EmergencyContactEntity
import com.example.data.local.entities.IncidentEntity
import com.example.data.local.entities.JourneyEntity
import com.example.data.local.entities.PendingSyncEntity
import com.example.data.local.entities.ProfileEntity
import com.example.data.local.entities.ResponderAssignmentEntity
import com.example.data.local.entities.ResponderProfileEntity
import com.example.data.local.entities.UserSessionEntity
import com.example.domain.model.IncidentStatus
import com.example.domain.model.JourneyStatus
import com.example.domain.model.RiskLevel

class Converters {
    @TypeConverter
    fun fromJourneyStatus(status: JourneyStatus): String = status.name

    @TypeConverter
    fun toJourneyStatus(value: String): JourneyStatus = runCatching {
        JourneyStatus.valueOf(value)
    }.getOrDefault(JourneyStatus.IDLE)

    @TypeConverter
    fun fromIncidentStatus(status: IncidentStatus): String = status.name

    @TypeConverter
    fun toIncidentStatus(value: String): IncidentStatus = runCatching {
        IncidentStatus.valueOf(value)
    }.getOrDefault(IncidentStatus.DETECTED)

    @TypeConverter
    fun fromRiskLevel(risk: RiskLevel): String = risk.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = runCatching {
        RiskLevel.valueOf(value)
    }.getOrDefault(RiskLevel.LOW)
}

@Database(
    entities = [
        ProfileEntity::class,
        EmergencyContactEntity::class,
        JourneyEntity::class,
        IncidentEntity::class,
        UserSessionEntity::class,
        ResponderProfileEntity::class,
        ResponderAssignmentEntity::class,
        PendingSyncEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SaarathiDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun journeyDao(): JourneyDao
    abstract fun incidentDao(): IncidentDao
    abstract fun userSessionDao(): UserSessionDao
    abstract fun responderDao(): ResponderDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        @Volatile
        private var INSTANCE: SaarathiDatabase? = null

        fun getDatabase(context: Context): SaarathiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SaarathiDatabase::class.java,
                    "saarathi_golden_hour.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
