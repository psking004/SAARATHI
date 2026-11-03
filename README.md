# SAARATHI — Accident Detection & Emergency Coordination (Android prototype)

> **"Bridging the Golden Hour."** Detect a possible road accident from phone sensors, confirm it with the rider, and help connect them to the right help during the first 60 minutes.

SAARATHI is an **offline-first Android prototype** written in Kotlin and Jetpack Compose. It reads the phone's accelerometer, gyroscope and GPS during a journey, runs a **rule-based crash-detection engine**, and starts a 10-second "Are you okay?" countdown before escalating to an emergency state with one-tap dialling of Indian emergency numbers, first-aid guidance and nearby-responder matching.

It is **not** a replacement for 112/108 or any emergency service, it does not guarantee crash detection, and it does not dispatch ambulances.

## Status at a glance

| Area | Status |
|---|---|
| Rule-based crash detection (G-force, rotation, speed delta, jerk) | ✅ Implemented, unit-tested |
| 10-second confirmation countdown and emergency state machine | ✅ Implemented, unit-tested |
| One-tap dialling of 112 / 108 / 100 / 101 and emergency contacts | ✅ Implemented (opens the phone dialler) |
| First-aid & scene-safety guidance in English, Hindi, Kannada, Tamil | ✅ Implemented, unit-tested |
| Good Samaritan legal-protection info screen | ✅ Implemented (static content) |
| Local Room database (profile, contacts, journeys, incidents) | ✅ Implemented |
| Nearby-responder matching (Haversine, 5 km radius) | ✅ Implemented against a built-in sample responder pool |
| Local system notifications (alert, responder, status) | ✅ Implemented |
| Login / account | ⚠️ **Local offline profile only.** No server authentication; "sign in" creates or restores a profile stored on the device |
| Map | ⚠️ Custom Compose Canvas map view; the Mapbox SDK is **not** integrated (only a token reader exists) |
| Supabase cloud sync | 🕒 **Planned.** Config reader, SQL migration and a local pending-sync queue exist; nothing is uploaded yet |
| SMS / push alerts to emergency contacts and responders | 🕒 **Planned.** Only local notifications on this phone are shown today |
| ML-based crash detection | 🕒 **Planned.** `MLDetectionEngineStub` currently delegates to the rule-based engine |

## Features

### Journey monitoring & detection
- Start/end a journey; the sensor pipeline reads the accelerometer and gyroscope (`SensorManager`) and location (`LocationManager`) while a journey is active.
- Live readout of acceleration (X/Y/Z, total G), rotation rate, speed, jerk and GPS accuracy.
- `RuleBasedDetectionEngine` classifies telemetry as low / medium / high risk and separates phone drops and normal bumps from likely crashes.
- Test panel with simulated scenarios: Normal Cruising, Phone Fall, Sudden Braking, Rollover Anomaly, High-G Crash.

### Confirmation & escalation
- On a high-risk reading: a 10-second countdown with a high-priority local notification.
- **I'M OKAY** dismisses it and records a false alarm; **I NEED HELP** or timeout escalates to the active-emergency state.
- Escalation shows a local notification, lists your emergency contacts for one-tap calling, and searches the sample responder pool.

### Profile & emergency contacts (stored on the device)
- Local profile: name, email, phone, blood group, medical notes. All fields are entered by the user; nothing is pre-filled.
- Emergency contacts with priority order and active/inactive toggle.

### Community responders
- Register as a responder (CPR-trained, first-aid, doctor, volunteer) on this device.
- Haversine distance matching within 5 km, with Accept/Decline and status tracking.

### First-aid & legal guidance
- Step-by-step guidance for an unconscious person, severe bleeding, fractures, vehicle hazards, fire/smoke and roadside perimeter safety, in four languages.
- Good Samaritan protections (Section 134A, Motor Vehicles Act, and Supreme Court guidelines) as an information screen.

## Architecture

MVVM with a single `MainViewModel`, Room for persistence, and Jetpack Compose for UI. Package: `com.example`.

```
com.example/
├── MainActivity.kt
├── data/
│   ├── local/              # Room database, DAOs, entities (incl. PendingSyncEntity queue)
│   ├── notification/       # NotificationProvider (local Android notifications)
│   ├── remote/             # SupabaseConfig, MapboxConfig (read keys from BuildConfig; no network calls yet)
│   ├── repository/         # Auth (local), Profile, Contacts, Journey, Incident, OfflineSync repositories
│   └── sensors/            # SensorPipeline (accelerometer, gyroscope, GPS, simulation injection)
├── domain/
│   ├── detection/          # DetectionEngine interface, RuleBasedDetectionEngine, MLDetectionEngineStub
│   ├── model/              # Domain models, EmergencyStateMachine
│   ├── responder/          # NearbyResponderService (Haversine matching, sample pool)
│   └── safety/             # Multi-language first-aid guidance, Good Samaritan content
└── ui/
    ├── MainViewModel.kt
    ├── components/         # Canvas map view, shared components
    ├── screens/            # Auth, Dashboard, Journey, Incident, Responder, SafetyGuidance, GoodSamaritan, Management
    └── theme/
```

Other folders: `supabase/migrations/` (draft Postgres schema for the planned sync), `docs/architecture.md` (design notes).

## Tech stack

Kotlin 2.2, Jetpack Compose (Material 3), Navigation Compose, Room 2.7 (KSP), Coroutines/Flow, Android sensor & location APIs. Tests: JUnit, Robolectric, Roborazzi (screenshot), kotlinx-coroutines-test. Build: Gradle 9.3 with AGP 9.1, JDK 17, minSdk 24 / targetSdk 36.

Retrofit, OkHttp, Moshi, DataStore and Play Services Location are declared as dependencies for the planned cloud and location work but are not used yet.

## Tests

Local JVM tests in `app/src/test`:
- `DetectionEngineTest`: crash heuristics, phone-drop handling, severe-impact alerts
- `EmergencyStateMachineTest`: valid and invalid state transitions
- `NearbyResponderServiceTest`: Haversine distance and candidate selection
- `SafetyGuidanceRepositoryTest`: guidance completeness across languages
- `SaarathiDatabaseAndModelsTest`: Room CRUD (Robolectric)
- `GreetingScreenshotTest`: Roborazzi screenshot test

## Build & run

```bash
./gradlew :app:assembleDebug        # debug APK
./gradlew :app:testDebugUnitTest    # unit + Robolectric tests
```

Optional keys go in `.env` (see `.env.example`) and are injected through the Secrets Gradle plugin. Without them the app runs in local-only mode.

## Roadmap

1. Supabase Auth and sync for profiles, contacts and incidents (drain the pending-sync queue).
2. SMS to emergency contacts and push notifications to nearby responders.
3. A real responder registry instead of the sample pool.
4. Mapbox SDK map with live location.
5. An ML crash classifier behind the existing `DetectionEngine` interface.
