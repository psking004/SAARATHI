# SAARATHI: Bridging the Golden Hour

> "We don't just detect an accident. We connect the right help to the right place during the Golden Hour."

## Overview
**SAARATHI** is a mobile emergency coordination platform. It is **NOT** an ambulance replacement and does not replace emergency dispatch services (112, 108, Police, Hospitals). Instead, it forms an intelligent coordination layer bridging:

```
Accident → Victim / Bystander → Emergency Services (112/108) → Nearby Willing Responders → Professional Hospital Care
```

---

## 1. System Architecture (Prompt 1/3 Foundation)

### Core Modules
1. **Authentication & Session Manager (`AuthRepository`)**:
   - Manages registration, secure login, persistent local sessions, and user state restoration.
   - Syncs profile data and security tokens with Supabase Auth / PostgreSQL backend.

2. **User Profile & Medical ID (`ProfileRepository`, `ProfileEntity`)**:
   - Critical triage data: Blood group, emergency medical notes, allergies, contact phone number.

3. **Emergency Contacts Management (`EmergencyContactRepository`, `EmergencyContactEntity`)**:
   - Multi-tiered priority contacts (Priority 1, 2, 3) with direct dial, SMS broadcast, and activation toggles.

4. **Journey Protection Engine (`JourneyRepository`, `JourneyEntity`)**:
   - Tracks lifecycle: `IDLE` → `ACTIVE` → `COMPLETED` / `CANCELLED`.
   - Records start/end timestamps and GPS coordinates.

5. **Incident Detection Architecture (`DetectionEngine`, `RuleBasedDetectionEngine`)**:
   - Pluggable interface accepting 3-axis accelerometer, gyroscope, speed, heading, and GPS telemetry.
   - Computes multi-factor impact severity, confidence scores, and triggers `IncidentConfirmation` countdowns.
   - Prepared for `MLDetectionEngine` integration in future phases.

6. **Golden Hour Emergency Coordination (`IncidentEntity`, Emergency State View)**:
   - High-priority state machine: `DETECTED` → `AWAITING_CONFIRMATION` → `CONFIRMED` → `ESCALATED` → `RESOLVED`.
   - 15-second automatic countdown before escalating to emergency dispatch.
   - Direct emergency dispatch quick dials (112, 108, 100).
   - Step-by-step Golden Hour survival and first responder actions.

---

## 2. Database Schema & RLS

- **`profiles`**: User metadata, blood group, medical history, phone.
- **`emergency_contacts`**: User emergency contacts with priority ranking.
- **`journeys`**: Active and historic travel telemetry metadata.
- **`incidents`**: Incident records, risk classifications (`LOW`, `MEDIUM`, `HIGH`), and timestamps.
- **Row-Level Security (RLS)**: Enforced on all tables via PostgreSQL `auth.uid()` predicates.

---

## 3. Brand & Design Tokens

- **Navy**: `#0B1F33` (Calm authority, main surfaces)
- **Teal**: `#00A6A6` (Active protection, beacon highlights)
- **Blue**: `#2F80ED` (Informational accents)
- **Emergency Red**: `#E53935` (*Restricted strictly to emergency states*)
- **Warning Amber**: `#F59E0B` (Incident confirmation countdowns)
- **Success Green**: `#22C55E` (Safe status, confirmation)
- **Background**: `#F7F9FC`
- **Surface**: `#FFFFFF`
- **Primary Text**: `#102A43`
- **Secondary Text**: `#627D98`
