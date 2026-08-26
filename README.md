# SAARATHI — Mobile Emergency Assistance & Coordination Platform

> **"Bridging the Golden Hour."**
> *"We don't just detect an accident. We connect the right help to the right place during the Golden Hour."*

---

## 🛡️ Core Philosophy & Safety Guarantees

SAARATHI is an **assistance and coordination platform** engineered to minimize delay in the first 60 minutes following a severe traumatic accident.

### Absolute Transparency Principles:
- **No Guaranteed Predictions:** The system does NOT claim 100% crash detection, guaranteed ambulance dispatch, or professional medical diagnosis.
- **Deterministic Heuristics (Zero ML Hype):** Evaluates multi-sensor telemetry (G-force magnitude, rotation rates, speed deltas, deceleration jerk) deterministically. Phone drops and nominal driving bumps are safely disambiguated without false emergency triggers.
- **Real Emergency Integration:** Direct calling capability for national helplines (**112** Unified Emergency, **108** Trauma/Ambulance, **100** Police, **101** Fire). No fabricated dispatch APIs.
- **Privacy-Preserving Dispatch:** Responders only receive approximate landmark areas until verified. Exact victim medical history is protected.
- **Offline-First Resilience:** Incident data and sensor waypoints are cached in SQLite Room and synchronized via `OfflineSyncRepository` when network connectivity returns.

---

## 🧩 Architectural Overview

The application is architected according to **MVVM + Clean Architecture**:

```
com.example/
├── domain/
│   ├── detection/          # RuleBasedDetectionEngine & heuristic crash algorithms
│   ├── emergency/          # EmergencyStateMachine & Golden Hour state progression
│   ├── model/              # Domain models (Incident, Journey, Responder, Telemetry)
│   ├── notification/       # Multi-channel notification dispatchers (SMS, Push, Call, SysNotif)
│   ├── responder/          # NearbyResponderService (Haversine candidate matching)
│   ├── safety/             # Multi-lingual safety guidance & Good Samaritan law repository
│   └── telemetry/          # SensorPipeline, GPS status, and mock test harnesses
├── data/
│   ├── local/              # Room database (RoomAppDatabase, DAOs, SyncQueue)
│   ├── remote/             # Supabase & Mapbox client configurations
│   ├── repository/         # Clean repository layer with offline fallback
│   └── sync/               # OfflineSyncRepository with exponential backoff queue
├── ui/
│   ├── screens/            # Jetpack Compose UI Screens (Clean Utility / Minimal Theme)
│   │   ├── DashboardScreen.kt
│   │   ├── JourneyActiveScreen.kt
│   │   ├── IncidentScreens.kt
│   │   ├── ResponderScreens.kt
│   │   ├── SafetyGuidanceScreen.kt
│   │   ├── GoodSamaritanScreen.kt
│   │   ├── ManagementScreens.kt
│   │   └── AuthScreens.kt
│   ├── theme/              # Material 3 Color Schemes & Typography
│   └── MainViewModel.kt    # Unified UI State Orchestration & State Flow
```

---

## ✨ Features Implemented

### 1. 🔐 Authentication & Profile
- Full registration (Name, Email, Phone, Blood Group, Medical Notes) and secure session management.
- Medical ID card and Emergency Contact management with priority ordering and toggleable active status.

### 2. 🗺️ Mapbox & Real Journey Telemetry
- Integrated Mapbox vector maps visualizing current position, live route breadcrumbs, and incident coordinates.
- Real-time sensor readout display showing accelerometer (X/Y/Z, total G-force), gyroscope (DPS), speed (km/h), jerk rate (G/s), and GPS accuracy.
- Testing panel with preset physical scenarios: *Normal Cruising, Phone Fall, Sudden Braking, Rollover Anomaly, High-G Impact*.

### 3. 🚨 10-Second Confirmation & Escalation Loop
- 10-second auditory and visual countdown upon detecting a severe impact.
- **"I'M OKAY"**: Dismisses the incident, returns to normal monitoring, and logs the false alarm.
- **"I NEED HELP"** / **Timeout**: Instantly escalates to active emergency state, triggers loud system alert notification, notifies priority emergency contacts, and activates responder search.

### 4. 🤝 Community First-Responder Network
- Voluntary registration as a certified community responder (CPR-trained, First-Aid, Doctor, or Volunteer).
- Haversine distance-based discovery for responders within a 5 km radius.
- Responder assignment dashboard with Accept/Decline actions, navigation to target area, and status tracking.

### 5. 🩹 Interactive First-Aid & Scene Safety Guidance
- Step-by-step emergency guidance for:
  1. Unconscious Person (airway, recovery position, helmet precautions)
  2. Severe Bleeding Control (direct continuous pressure, elevation)
  3. Bone & Joint Fractures (splinting, immobilization)
  4. Vehicle Hazards (ignition cutoff, hazard lights, fuel leaks)
  5. Fire & Smoke Containment (upwind evacuation, extinguisher rules)
  6. Roadside Collision Perimeter (warning triangles, lane defense)
- **Multi-language support:** English, Hindi (हिन्दी), Kannada (ಕನ್ನಡ), and Tamil (தமிழ்).
- **Simulated audio read-out** for hands-free step guidance during high-stress moments.

### 6. ⚖️ Good Samaritan Legal Protection Shield
- Full statutory legal protections under **Section 134A of the Motor Vehicles Act** and Supreme Court guidelines:
  - Zero civil or criminal liability for assisting in good faith.
  - Complete protection against forced police interrogation or identity disclosure.
  - Hospitals legally barred from demanding advance deposits before emergency treatment.
  - Information on the MoRTH National Good Samaritan Cash Reward Scheme.

### 7. 📶 Offline Resilience
- Local SQLite Room persistence for all emergency data.
- Pending sync queue (`PendingSyncEntity`) records failed network syncs and retries with backoff upon reconnection.

---

## 🧪 Testing & Verification

Comprehensive local JVM test coverage across all domains:
- `DetectionEngineTest`: Verification of crash detection heuristics, phone drop disambiguation, and severe collision alerts.
- `EmergencyStateMachineTest`: Verification of valid and invalid state transitions and listener callbacks.
- `NearbyResponderServiceTest`: Haversine distance calculations and candidate selection.
- `SafetyGuidanceRepositoryTest`: Verification of multi-language guidance and step completeness.
- `SaarathiDatabaseAndModelsTest`: Room database CRUD and local persistence testing.

---

## 🛠️ Build & Run

```bash
# Compile and build debug APK
gradle :app:assembleDebug

# Execute unit and Robolectric tests
gradle :app:testDebugUnitTest
```
