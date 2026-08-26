package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SaarathiDatabase
import com.example.data.notification.AndroidSystemNotificationProvider
import com.example.data.notification.NotificationProvider
import com.example.data.repository.AuthRepository
import com.example.data.repository.EmergencyContactRepository
import com.example.data.repository.IncidentRepository
import com.example.data.repository.JourneyRepository
import com.example.data.repository.OfflineSyncRepository
import com.example.data.repository.ProfileRepository
import com.example.data.repository.ResponderRepository
import com.example.data.sensors.GpsStatus
import com.example.data.sensors.SensorPipeline
import com.example.domain.detection.DetectionEngine
import com.example.domain.detection.DetectionResult
import com.example.domain.detection.RuleBasedDetectionEngine
import com.example.domain.detection.SensorTelemetry
import com.example.domain.model.AssignmentStatus
import com.example.domain.model.EmergencyContact
import com.example.domain.model.EmergencyState
import com.example.domain.model.EmergencyStateMachine
import com.example.domain.model.Incident
import com.example.domain.model.IncidentStatus
import com.example.domain.model.Journey
import com.example.domain.model.LocationPoint
import com.example.domain.model.MovementState
import com.example.domain.model.NotificationDeliveryResult
import com.example.domain.model.RegionalLanguage
import com.example.domain.model.ResponderAssignment
import com.example.domain.model.ResponderProfile
import com.example.domain.model.ResponderType
import com.example.domain.model.RiskLevel
import com.example.domain.model.UserProfile
import com.example.domain.model.UserSession
import com.example.domain.model.VerificationStatus
import com.example.domain.responder.NearbyResponderService
import com.example.domain.safety.SafetyCategory
import com.example.domain.safety.SafetyGuidanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

data class MediumRiskAlert(
    val reason: String,
    val signals: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SaarathiDatabase.getDatabase(application)
    val authRepository = AuthRepository(db.userSessionDao(), db.profileDao())
    val profileRepository = ProfileRepository(db.profileDao())
    val contactRepository = EmergencyContactRepository(db.emergencyContactDao())
    val journeyRepository = JourneyRepository(db.journeyDao())
    val incidentRepository = IncidentRepository(db.incidentDao())
    val responderRepository = ResponderRepository(db.responderDao())
    val offlineSyncRepository = OfflineSyncRepository(db.pendingSyncDao())

    val notificationProvider: NotificationProvider = AndroidSystemNotificationProvider(application.applicationContext)
    val emergencyStateMachine = EmergencyStateMachine(EmergencyState.NORMAL)

    val detectionEngine: DetectionEngine = RuleBasedDetectionEngine()
    val sensorPipeline = SensorPipeline(application.applicationContext, viewModelScope)

    // Emergency State Machine Flow
    private val _emergencyState = MutableStateFlow(EmergencyState.NORMAL)
    val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    // Session & Auth state
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    private val _isSessionLoading = MutableStateFlow(true)
    val isSessionLoading: StateFlow<Boolean> = _isSessionLoading.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Responder Mode & Profile
    private val _isResponderMode = MutableStateFlow(false)
    val isResponderMode: StateFlow<Boolean> = _isResponderMode.asStateFlow()

    private val _responderProfile = MutableStateFlow<ResponderProfile?>(null)
    val responderProfile: StateFlow<ResponderProfile?> = _responderProfile.asStateFlow()

    private val _nearbyResponders = MutableStateFlow<List<Pair<ResponderProfile, Double>>>(emptyList())
    val nearbyResponders: StateFlow<List<Pair<ResponderProfile, Double>>> = _nearbyResponders.asStateFlow()

    private val _incomingResponderRequest = MutableStateFlow<ResponderAssignment?>(null)
    val incomingResponderRequest: StateFlow<ResponderAssignment?> = _incomingResponderRequest.asStateFlow()

    private val _activeAssignment = MutableStateFlow<ResponderAssignment?>(null)
    val activeAssignment: StateFlow<ResponderAssignment?> = _activeAssignment.asStateFlow()

    // Safety Guidance & Voice state
    private val _safetyLanguage = MutableStateFlow(RegionalLanguage.ENGLISH)
    val safetyLanguage: StateFlow<RegionalLanguage> = _safetyLanguage.asStateFlow()
    val selectedRegionalLanguage: StateFlow<RegionalLanguage> = _safetyLanguage.asStateFlow()

    private val _selectedSafetyCategory = MutableStateFlow(SafetyCategory.GENERAL_SCENE_SAFETY)
    val selectedSafetyCategory: StateFlow<SafetyCategory> = _selectedSafetyCategory.asStateFlow()

    private val _isVoicePlaying = MutableStateFlow(false)
    val isVoicePlaying: StateFlow<Boolean> = _isVoicePlaying.asStateFlow()

    private val _activeVoiceStepIndex = MutableStateFlow<Int?>(null)
    val activeVoiceStepIndex: StateFlow<Int?> = _activeVoiceStepIndex.asStateFlow()

    private val _activeVoiceStep = MutableStateFlow<String?>(null)
    val activeVoiceStep: StateFlow<String?> = _activeVoiceStep.asStateFlow()

    // Notification Log / Results
    private val _lastNotificationResult = MutableStateFlow<NotificationDeliveryResult?>(null)
    val lastNotificationResult: StateFlow<NotificationDeliveryResult?> = _lastNotificationResult.asStateFlow()

    // Emergency Contacts
    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    // Journey
    private val _activeJourney = MutableStateFlow<Journey?>(null)
    val activeJourney: StateFlow<Journey?> = _activeJourney.asStateFlow()

    private val _journeyHistory = MutableStateFlow<List<Journey>>(emptyList())
    val journeyHistory: StateFlow<List<Journey>> = _journeyHistory.asStateFlow()

    // Incidents
    private val _incidentHistory = MutableStateFlow<List<Incident>>(emptyList())
    val incidentHistory: StateFlow<List<Incident>> = _incidentHistory.asStateFlow()

    private val _activeIncident = MutableStateFlow<Incident?>(null)
    val activeIncident: StateFlow<Incident?> = _activeIncident.asStateFlow()

    // Incident Confirmation Countdown (10 seconds per Prompt 2)
    private val _countdownSeconds = MutableStateFlow(10)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    // Medium Risk Non-Intrusive Alert Banner state
    private val _mediumRiskAlert = MutableStateFlow<MediumRiskAlert?>(null)
    val mediumRiskAlert: StateFlow<MediumRiskAlert?> = _mediumRiskAlert.asStateFlow()

    private var countdownJob: Job? = null
    private var journeyTimerJob: Job? = null
    private var voiceGuidanceJob: Job? = null

    private val _journeyElapsedSeconds = MutableStateFlow(0L)
    val journeyElapsedSeconds: StateFlow<Long> = _journeyElapsedSeconds.asStateFlow()

    // UI Events
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // Live Telemetry Display State (Real hardware sensors or injected simulations)
    val latestTelemetry: StateFlow<SensorTelemetry> = sensorPipeline.telemetry
    val gpsStatus: StateFlow<GpsStatus> = sensorPipeline.gpsStatus
    val sampledWaypoints: StateFlow<List<LocationPoint>> = sensorPipeline.sampledWaypoints

    private val _latestDetection = MutableStateFlow<DetectionResult?>(null)
    val latestDetection: StateFlow<DetectionResult?> = _latestDetection.asStateFlow()

    init {
        emergencyStateMachine.addListener { _, newState ->
            _emergencyState.value = newState
        }
        restoreSessionAndSeedDefaults()
        observeSensorPipeline()
    }

    private fun observeSensorPipeline() {
        viewModelScope.launch {
            sensorPipeline.telemetry.collectLatest { telemetry ->
                if (_activeJourney.value != null) {
                    processIncomingTelemetry(telemetry)
                }
            }
        }
    }

    private fun restoreSessionAndSeedDefaults() {
        viewModelScope.launch {
            authRepository.activeSession.collectLatest { session ->
                _userSession.value = session
                _isSessionLoading.value = false
                if (session != null) {
                    loadUserData(session.userId)
                } else {
                    _userProfile.value = null
                    _emergencyContacts.value = emptyList()
                    _activeJourney.value = null
                    _responderProfile.value = null
                }
            }
        }
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            profileRepository.getProfile(userId).collectLatest { profile ->
                _userProfile.value = profile
            }
        }

        viewModelScope.launch {
            contactRepository.getContacts(userId).collectLatest { contacts ->
                _emergencyContacts.value = contacts
                if (contacts.isEmpty()) {
                    seedDefaultContacts(userId)
                }
            }
        }

        viewModelScope.launch {
            responderRepository.getResponderProfile(userId).collectLatest { profile ->
                _responderProfile.value = profile
                if (profile != null) {
                    NearbyResponderService.registerOrUpdateResponder(profile)
                }
            }
        }

        viewModelScope.launch {
            journeyRepository.getActiveJourney(userId).collectLatest { journey ->
                _activeJourney.value = journey
                if (journey != null && journey.startedAt != null) {
                    startJourneyTimer(journey.startedAt)
                    sensorPipeline.startTracking()
                } else {
                    stopJourneyTimer()
                    sensorPipeline.stopTracking()
                }
            }
        }

        viewModelScope.launch {
            journeyRepository.getJourneys(userId).collectLatest { list ->
                _journeyHistory.value = list
            }
        }

        viewModelScope.launch {
            incidentRepository.getIncidents(userId).collectLatest { list ->
                _incidentHistory.value = list
            }
        }
    }

    private suspend fun seedDefaultContacts(userId: String) {
        val default1 = EmergencyContact(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Ramesh Sharma (Father)",
            phone = "+91 98450 11223",
            relationship = "Parent",
            priority = 1,
            isActive = true
        )
        val default2 = EmergencyContact(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "Priya Sharma (Spouse)",
            phone = "+91 98450 99887",
            relationship = "Spouse",
            priority = 2,
            isActive = true
        )
        contactRepository.upsertContact(default1)
        contactRepository.upsertContact(default2)
    }

    // --- Authentication Actions ---

    fun register(
        fullName: String,
        email: String,
        phone: String,
        bloodType: String,
        medicalNotes: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (password != confirmPassword) {
            onError("Passwords do not match")
            return
        }
        viewModelScope.launch {
            val result = authRepository.signUp(
                fullName = fullName,
                email = email,
                phone = phone,
                bloodType = bloodType,
                medicalNotes = medicalNotes,
                password = password
            )
            result.onSuccess {
                _events.emit(UiEvent.ShowToast("Welcome to SAARATHI, ${it.fullName}!"))
                onSuccess()
            }.onFailure {
                onError(it.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            result.onSuccess {
                _events.emit(UiEvent.ShowToast("Welcome back, ${it.fullName}"))
                onSuccess()
            }.onFailure {
                onError(it.localizedMessage ?: "Login failed")
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            sensorPipeline.stopTracking()
            _userSession.value = null
            _userProfile.value = null
            _activeJourney.value = null
            _activeIncident.value = null
            _mediumRiskAlert.value = null
            _responderProfile.value = null
            _isResponderMode.value = false
            stopCountdown()
            stopJourneyTimer()
            stopVoiceGuidance()
            emergencyStateMachine.reset()
            _events.emit(UiEvent.ShowToast("Logged out successfully"))
            onComplete()
        }
    }

    // --- Profile Management ---

    fun updateProfile(
        fullName: String,
        phone: String,
        bloodType: String,
        medicalNotes: String,
        onSuccess: () -> Unit
    ) {
        val current = _userProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                fullName = fullName.trim().ifBlank { current.fullName },
                phone = phone.trim(),
                bloodType = bloodType.trim(),
                medicalNotes = medicalNotes.trim(),
                updatedAt = System.currentTimeMillis()
            )
            profileRepository.updateProfile(updated)
            _events.emit(UiEvent.ShowToast("Emergency profile updated"))
            onSuccess()
        }
    }

    // --- Responder System Management ---

    fun toggleResponderMode(enabled: Boolean) {
        _isResponderMode.value = enabled
        if (enabled && _responderProfile.value == null) {
            // Seed initial responder profile for current user
            val current = _userProfile.value
            val userId = _userSession.value?.userId ?: "user-local"
            val newProfile = ResponderProfile(
                id = "resp-${UUID.randomUUID().toString().take(6)}",
                userId = userId,
                name = current?.fullName ?: "Good Samaritan Responder",
                phone = current?.phone ?: "+91 98450 00112",
                type = ResponderType.CPR_TRAINED,
                verificationStatus = VerificationStatus.COMMUNITY_VERIFIED,
                languages = listOf("English", "Hindi", "Kannada"),
                isAvailable = true,
                trainingDetails = "St. John Ambulance Certified First Responder (CPR & First Aid)",
                latitude = 12.9716,
                longitude = 77.5946,
                address = "Central Bengaluru Sector"
            )
            viewModelScope.launch {
                responderRepository.upsertResponderProfile(newProfile)
                NearbyResponderService.registerOrUpdateResponder(newProfile)
                _responderProfile.value = newProfile
            }
        }
    }

    fun updateResponderProfile(
        name: String,
        phone: String,
        type: ResponderType,
        languages: List<String>,
        trainingDetails: String,
        address: String,
        isAvailable: Boolean,
        onSuccess: () -> Unit
    ) {
        val userId = _userSession.value?.userId ?: return
        val current = _responderProfile.value
        val updated = ResponderProfile(
            id = current?.id ?: "resp-${UUID.randomUUID().toString().take(6)}",
            userId = userId,
            name = name.trim(),
            phone = phone.trim(),
            type = type,
            verificationStatus = current?.verificationStatus ?: VerificationStatus.COMMUNITY_VERIFIED,
            languages = languages,
            isAvailable = isAvailable,
            trainingDetails = trainingDetails.trim(),
            latitude = current?.latitude ?: 12.9716,
            longitude = current?.longitude ?: 77.5946,
            address = address.trim(),
            rating = current?.rating ?: 5.0f,
            totalResponses = current?.totalResponses ?: 0
        )

        viewModelScope.launch {
            responderRepository.upsertResponderProfile(updated)
            NearbyResponderService.registerOrUpdateResponder(updated)
            _responderProfile.value = updated
            _events.emit(UiEvent.ShowToast("Responder profile updated"))
            onSuccess()
        }
    }

    fun toggleResponderAvailability(isAvailable: Boolean) {
        val current = _responderProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(isAvailable = isAvailable)
            responderRepository.upsertResponderProfile(updated)
            NearbyResponderService.registerOrUpdateResponder(updated)
            _responderProfile.value = updated
            _events.emit(UiEvent.ShowToast(if (isAvailable) "You are now Active & Available to respond." else "Responder status set to Offline."))
        }
    }

    fun acceptIncomingResponderRequest(onAccepted: () -> Unit) {
        val req = _incomingResponderRequest.value ?: return
        viewModelScope.launch {
            val accepted = req.copy(
                status = AssignmentStatus.ACCEPTED,
                respondedAt = System.currentTimeMillis()
            )
            responderRepository.saveAssignment(accepted)
            _activeAssignment.value = accepted
            _incomingResponderRequest.value = null
            notificationProvider.sendResponderAccepted(accepted)
            _events.emit(UiEvent.ShowToast("Emergency request accepted. Proceeding to location."))
            onAccepted()
        }
    }

    fun declineIncomingResponderRequest(onDeclined: () -> Unit) {
        val req = _incomingResponderRequest.value ?: return
        viewModelScope.launch {
            val declined = req.copy(
                status = AssignmentStatus.DECLINED,
                respondedAt = System.currentTimeMillis()
            )
            responderRepository.saveAssignment(declined)
            _incomingResponderRequest.value = null
            _events.emit(UiEvent.ShowToast("Request declined. Re-routing to other responders."))
            onDeclined()
        }
    }

    fun completeActiveAssignment(onCompleted: () -> Unit) {
        val current = _activeAssignment.value ?: return
        viewModelScope.launch {
            responderRepository.updateAssignmentStatus(current.id, AssignmentStatus.COMPLETED)
            _activeAssignment.value = null
            _events.emit(UiEvent.ShowToast("Response mission completed. Thank you for your service."))
            onCompleted()
        }
    }

    // --- Safety Guidance & Voice Readout ---

    fun setSafetyLanguage(language: RegionalLanguage) {
        _safetyLanguage.value = language
    }

    fun setRegionalLanguage(language: RegionalLanguage) {
        _safetyLanguage.value = language
    }

    fun setSelectedSafetyCategory(category: SafetyCategory) {
        _selectedSafetyCategory.value = category
    }

    fun selectSafetyCategory(category: SafetyCategory) {
        _selectedSafetyCategory.value = category
    }

    fun playVoiceGuidance(text: String) {
        playVoiceGuidanceStep(0, text)
    }

    fun playVoiceGuidanceStep(stepNumber: Int, textToSpeak: String) {
        voiceGuidanceJob?.cancel()
        _isVoicePlaying.value = true
        _activeVoiceStepIndex.value = stepNumber
        _activeVoiceStep.value = textToSpeak

        voiceGuidanceJob = viewModelScope.launch {
            val durationMs = (textToSpeak.length * 45L).coerceIn(3000L, 8000L)
            delay(durationMs)
            _isVoicePlaying.value = false
            _activeVoiceStepIndex.value = null
            _activeVoiceStep.value = null
        }
    }

    fun stopVoiceGuidance() {
        voiceGuidanceJob?.cancel()
        _isVoicePlaying.value = false
        _activeVoiceStepIndex.value = null
        _activeVoiceStep.value = null
    }

    // --- Emergency Contacts Management ---

    fun saveContact(
        id: String?,
        name: String,
        phone: String,
        relationship: String,
        priority: Int,
        isActive: Boolean = true,
        onComplete: () -> Unit
    ) {
        val userId = _userSession.value?.userId ?: return
        viewModelScope.launch {
            val contact = EmergencyContact(
                id = id ?: UUID.randomUUID().toString(),
                userId = userId,
                name = name.trim(),
                phone = phone.trim(),
                relationship = relationship.trim(),
                priority = priority,
                isActive = isActive
            )
            contactRepository.upsertContact(contact)
            _events.emit(UiEvent.ShowToast("Emergency contact saved"))
            onComplete()
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            contactRepository.deleteContact(contactId)
            _events.emit(UiEvent.ShowToast("Contact removed"))
        }
    }

    fun toggleContactActive(contact: EmergencyContact) {
        viewModelScope.launch {
            contactRepository.upsertContact(contact.copy(isActive = !contact.isActive))
        }
    }

    // --- Journey Management ---

    fun startJourney(
        startLat: Double = 12.9716,
        startLng: Double = 77.5946,
        onStarted: () -> Unit
    ) {
        val userId = _userSession.value?.userId ?: return
        viewModelScope.launch {
            val journey = journeyRepository.startJourney(userId, startLat, startLng)
            _activeJourney.value = journey
            sensorPipeline.startTracking()
            emergencyStateMachine.transitionTo(EmergencyState.NORMAL)
            _events.emit(UiEvent.ShowToast("Journey protection armed."))
            onStarted()
        }
    }

    fun endJourney(onEnded: () -> Unit) {
        val currentJourney = _activeJourney.value ?: return
        viewModelScope.launch {
            sensorPipeline.stopTracking()
            val latestLoc = sensorPipeline.telemetry.value
            journeyRepository.endJourney(
                journeyId = currentJourney.id,
                endLat = latestLoc.latitude ?: 12.9716,
                endLng = latestLoc.longitude ?: 77.5946
            )
            _activeJourney.value = null
            _mediumRiskAlert.value = null
            stopJourneyTimer()
            emergencyStateMachine.transitionTo(EmergencyState.NORMAL)
            _events.emit(UiEvent.ShowToast("Journey safely completed."))
            onEnded()
        }
    }

    private fun startJourneyTimer(startedAt: Long) {
        journeyTimerJob?.cancel()
        journeyTimerJob = viewModelScope.launch {
            while (true) {
                val elapsed = (System.currentTimeMillis() - startedAt) / 1000L
                _journeyElapsedSeconds.value = elapsed.coerceAtLeast(0L)
                delay(1000)
            }
        }
    }

    private fun stopJourneyTimer() {
        journeyTimerJob?.cancel()
        _journeyElapsedSeconds.value = 0L
    }

    // --- Detection & Incident Telemetry Processing ---

    private fun processIncomingTelemetry(telemetry: SensorTelemetry) {
        val result = detectionEngine.evaluate(telemetry)
        _latestDetection.value = result

        // Check Risk Level branches
        when (result.riskLevel) {
            RiskLevel.HIGH -> {
                if (_activeIncident.value == null) {
                    _mediumRiskAlert.value = null
                    triggerIncidentCandidate(
                        riskLevel = RiskLevel.HIGH,
                        probability = result.crashProbability,
                        reason = result.reason,
                        lat = telemetry.latitude ?: 12.9716,
                        lng = telemetry.longitude ?: 77.5946
                    )
                }
            }
            RiskLevel.MEDIUM -> {
                // Non-intrusive banner / prompt for Medium risk
                if (_activeIncident.value == null && _mediumRiskAlert.value == null) {
                    _mediumRiskAlert.value = MediumRiskAlert(
                        reason = result.reason,
                        signals = result.signals,
                        timestamp = result.timestamp
                    )
                }
            }
            RiskLevel.LOW -> {
                // Nominal or Phone drop disambiguation — no escalation
            }
        }
    }

    fun dismissMediumRiskAlert(continueMonitoring: Boolean = true) {
        _mediumRiskAlert.value = null
        if (!continueMonitoring) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowToast("Alert cleared. Safe status maintained."))
            }
        }
    }

    // --- Testing & Simulation Presets ---

    fun triggerSimulation(testCase: String) {
        val baseLat = 12.9716
        val baseLng = 77.5946
        val now = System.currentTimeMillis()

        val syntheticTelemetry = when (testCase) {
            "PHONE_FALL" -> {
                SensorTelemetry(
                    accelerometerX = 16.5f,
                    accelerometerY = 22.0f,
                    accelerometerZ = 12.0f,
                    totalGForce = 2.9f,
                    gyroscopeX = 3.5f,
                    gyroscopeY = 4.2f,
                    gyroscopeZ = 1.8f,
                    rotationRateDps = 320f,
                    jerkGPerSec = 8.5f,
                    speedKmh = 55f,
                    speedDeltaKmh = 2f,
                    headingDegrees = 45f,
                    latitude = baseLat,
                    longitude = baseLng,
                    movementState = MovementState.CRUISING,
                    timestamp = now
                )
            }
            "SUDDEN_BRAKING" -> {
                SensorTelemetry(
                    accelerometerX = 2.0f,
                    accelerometerY = -16.0f,
                    accelerometerZ = 9.8f,
                    totalGForce = 1.9f,
                    gyroscopeX = 0.2f,
                    gyroscopeY = 0.4f,
                    gyroscopeZ = 0.1f,
                    rotationRateDps = 25f,
                    jerkGPerSec = 4.0f,
                    speedKmh = 12f,
                    speedDeltaKmh = 48f,
                    headingDegrees = 90f,
                    latitude = baseLat,
                    longitude = baseLng,
                    movementState = MovementState.HARD_BRAKING,
                    timestamp = now
                )
            }
            "ROLLOVER_ANOMALY" -> {
                SensorTelemetry(
                    accelerometerX = 14.0f,
                    accelerometerY = -18.0f,
                    accelerometerZ = 12.0f,
                    totalGForce = 2.7f,
                    gyroscopeX = 4.8f,
                    gyroscopeY = 3.2f,
                    gyroscopeZ = 2.1f,
                    rotationRateDps = 360f,
                    jerkGPerSec = 12.0f,
                    speedKmh = 20f,
                    speedDeltaKmh = 25f,
                    headingDegrees = 180f,
                    latitude = baseLat,
                    longitude = baseLng,
                    movementState = MovementState.TURNING,
                    timestamp = now
                )
            }
            "HIGH_G_CRASH" -> {
                SensorTelemetry(
                    accelerometerX = 28.5f,
                    accelerometerY = -42.0f,
                    accelerometerZ = 22.0f,
                    totalGForce = 5.2f,
                    gyroscopeX = 5.6f,
                    gyroscopeY = 4.8f,
                    gyroscopeZ = 3.2f,
                    rotationRateDps = 440f,
                    jerkGPerSec = 28.0f,
                    speedKmh = 0f,
                    speedDeltaKmh = 65f,
                    headingDegrees = 110f,
                    latitude = baseLat,
                    longitude = baseLng,
                    movementState = MovementState.ABNORMAL_IMPACT,
                    timestamp = now
                )
            }
            else -> { // NORMAL_CRUISING
                SensorTelemetry(
                    accelerometerX = 0.2f,
                    accelerometerY = -0.4f,
                    accelerometerZ = 9.8f,
                    totalGForce = 1.0f,
                    gyroscopeX = 0.02f,
                    gyroscopeY = 0.01f,
                    gyroscopeZ = 0.01f,
                    rotationRateDps = 8f,
                    jerkGPerSec = 0.5f,
                    speedKmh = 45f,
                    speedDeltaKmh = 0f,
                    headingDegrees = 20f,
                    latitude = baseLat,
                    longitude = baseLng,
                    movementState = MovementState.CRUISING,
                    timestamp = now
                )
            }
        }

        sensorPipeline.injectTelemetry(syntheticTelemetry)
        processIncomingTelemetry(syntheticTelemetry)
    }

    fun triggerManualSOS() {
        val currentLoc = sensorPipeline.telemetry.value
        triggerIncidentCandidate(
            riskLevel = RiskLevel.HIGH,
            probability = 1.0f,
            reason = "Manual Emergency SOS Triggered by User",
            lat = currentLoc.latitude ?: 12.9716,
            lng = currentLoc.longitude ?: 77.5946,
            skipCountdown = true
        )
    }

    private fun triggerIncidentCandidate(
        riskLevel: RiskLevel,
        probability: Float,
        reason: String,
        lat: Double,
        lng: Double,
        skipCountdown: Boolean = false
    ) {
        val userId = _userSession.value?.userId ?: return
        val journeyId = _activeJourney.value?.id

        viewModelScope.launch {
            val incident = Incident(
                id = UUID.randomUUID().toString(),
                userId = userId,
                journeyId = journeyId,
                status = if (skipCountdown) IncidentStatus.CONFIRMED else IncidentStatus.AWAITING_CONFIRMATION,
                riskLevel = riskLevel,
                crashProbability = probability,
                latitude = lat,
                longitude = lng,
                detectedAt = System.currentTimeMillis(),
                confirmedAt = if (skipCountdown) System.currentTimeMillis() else null,
                notes = reason
            )
            val saved = incidentRepository.createIncident(incident)
            _activeIncident.value = saved

            // Queue offline sync
            offlineSyncRepository.queueIncidentSync(saved)

            if (skipCountdown) {
                emergencyStateMachine.transitionTo(EmergencyState.EMERGENCY_ACTIVE)
                dispatchEmergencyCoordination(saved)
                _events.emit(UiEvent.Navigate("emergency_state"))
            } else {
                emergencyStateMachine.transitionTo(EmergencyState.POSSIBLE_INCIDENT)
                emergencyStateMachine.transitionTo(EmergencyState.CONFIRMATION_REQUIRED)
                startCountdown(saved)
                _events.emit(UiEvent.Navigate("incident_confirmation"))
            }
        }
    }

    private fun startCountdown(incident: Incident) {
        stopCountdown()
        _countdownSeconds.value = 10
        notificationProvider.sendEmergencyAlert(incident, 10)

        countdownJob = viewModelScope.launch {
            for (sec in 10 downTo 1) {
                _countdownSeconds.value = sec
                delay(1000)
            }
            _countdownSeconds.value = 0
            // No response within 10s: automatically escalate to emergency
            escalateIncidentDueToNoResponse()
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun dismissIncidentAsFalseAlarm(onDismissed: () -> Unit) {
        val current = _activeIncident.value ?: return
        stopCountdown()
        viewModelScope.launch {
            val updated = current.copy(
                status = IncidentStatus.CANCELLED,
                resolvedAt = System.currentTimeMillis(),
                notes = "${current.notes} (Dismissed by user: I'm Safe)"
            )
            incidentRepository.updateIncident(updated)
            _activeIncident.value = null
            emergencyStateMachine.transitionTo(EmergencyState.CANCELLED)
            emergencyStateMachine.transitionTo(EmergencyState.NORMAL)
            notificationProvider.sendIncidentResolved(updated.id)
            _events.emit(UiEvent.ShowToast("Incident dismissed. Stay safe!"))
            onDismissed()
        }
    }

    fun confirmIncidentImmediately(onConfirmed: () -> Unit) {
        stopCountdown()
        val current = _activeIncident.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                status = IncidentStatus.CONFIRMED,
                confirmedAt = System.currentTimeMillis(),
                notes = "${current.notes} (Confirmed: I Need Help Now)"
            )
            incidentRepository.updateIncident(updated)
            _activeIncident.value = updated
            emergencyStateMachine.transitionTo(EmergencyState.EMERGENCY_ACTIVE)
            dispatchEmergencyCoordination(updated)
            _events.emit(UiEvent.Navigate("emergency_state"))
            onConfirmed()
        }
    }

    private fun escalateIncidentDueToNoResponse() {
        val current = _activeIncident.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                status = IncidentStatus.CONFIRMED,
                confirmedAt = System.currentTimeMillis(),
                notes = "Emergency response initiated based on high-risk detection and no response."
            )
            incidentRepository.updateIncident(updated)
            _activeIncident.value = updated
            emergencyStateMachine.transitionTo(EmergencyState.EMERGENCY_ACTIVE)
            dispatchEmergencyCoordination(updated)
            _events.emit(UiEvent.Navigate("emergency_state"))
        }
    }

    /**
     * Complete Golden Hour Coordination Workflow:
     * 1. Broadcasts location & alert to active emergency contacts
     * 2. Searches nearby certified first responders & dispatches responder alert
     * 3. Escalates state machine to HELP_REQUESTED -> RESPONDER_FOUND
     * 4. Keeps professional 112/108 response as primary priority.
     */
    private fun dispatchEmergencyCoordination(incident: Incident) {
        viewModelScope.launch {
            emergencyStateMachine.transitionTo(EmergencyState.HELP_REQUESTED)

            val lat = incident.latitude ?: 12.9716
            val lng = incident.longitude ?: 77.5946
            val mapsUrl = "https://maps.google.com/?q=$lat,$lng"

            // 1. Notify Emergency Contacts
            val contactResult = notificationProvider.sendEmergencyContactBroadcast(
                contacts = _emergencyContacts.value,
                incident = incident,
                liveLocationUrl = mapsUrl
            )
            _lastNotificationResult.value = contactResult

            // 2. Query Nearby Responders (Haversine search)
            val nearby = NearbyResponderService.findNearbyResponders(
                incidentLat = lat,
                incidentLng = lng,
                maxRadiusMeters = 5000.0,
                incidentRisk = incident.riskLevel
            )
            _nearbyResponders.value = nearby

            // 3. Dispatch to best candidate if available
            if (nearby.isNotEmpty()) {
                val (bestResponder, distance) = nearby.first()
                val request = NearbyResponderService.createAssignmentRequest(
                    incident = incident,
                    responder = bestResponder,
                    distanceMeters = distance
                )
                _incomingResponderRequest.value = request
                notificationProvider.sendResponderRequest(request)
                emergencyStateMachine.transitionTo(EmergencyState.RESPONDER_FOUND)
            } else {
                emergencyStateMachine.transitionTo(EmergencyState.PROFESSIONAL_HELP_PENDING)
            }
        }
    }

    fun resolveEmergency(onResolved: () -> Unit) {
        val current = _activeIncident.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                status = IncidentStatus.RESOLVED,
                resolvedAt = System.currentTimeMillis(),
                notes = "${current.notes} (Resolved by user)"
            )
            incidentRepository.updateIncident(updated)
            _activeIncident.value = null
            emergencyStateMachine.transitionTo(EmergencyState.RESOLVED)
            emergencyStateMachine.transitionTo(EmergencyState.NORMAL)
            notificationProvider.sendIncidentResolved(updated.id)
            _events.emit(UiEvent.ShowToast("Emergency resolved. Safe status restored."))
            onResolved()
        }
    }
}
