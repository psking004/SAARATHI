package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sensors.GpsStatus
import com.example.domain.detection.DetectionResult
import com.example.domain.detection.SensorTelemetry
import com.example.domain.model.EmergencyContact
import com.example.domain.model.Journey
import com.example.domain.model.LocationPoint
import com.example.domain.model.MovementState
import com.example.ui.MediumRiskAlert
import com.example.ui.components.MapboxMapView
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.SaarathiBackground
import com.example.ui.theme.SaarathiBlue
import com.example.ui.theme.SaarathiNavy
import com.example.ui.theme.SaarathiTeal
import com.example.ui.theme.SaarathiTealLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer

@Composable
fun JourneySetupScreen(
    emergencyContacts: List<EmergencyContact>,
    onStartJourney: () -> Unit,
    onBack: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("City Commute") }
    val modes = listOf("City Commute", "Highway / Expressway", "Two Wheeler", "Night Travel")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("journey_setup_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaarathiNavy)
            }
            Text(
                text = "Journey Protection Setup",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaarathiNavy
                )
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Reusable Mapbox Map Preview
            MapboxMapView(
                userLatitude = 12.9716,
                userLongitude = 77.5946,
                headingDegrees = 0f,
                speedKmh = 0f,
                showControls = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Protection Readiness Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SaarathiNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SaarathiTeal.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = SaarathiTeal)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Golden Hour Protection Arming",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Sensors: 20Hz • Mapbox: Ready • Contacts: Armed",
                                color = SaarathiTealLight,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Transit Mode",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            modes.forEach { mode ->
                val isSelected = selectedMode == mode
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SaarathiTealLight else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedMode = mode }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mode.contains("Wheeler")) Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (isSelected) SaarathiTeal else SaarathiNavy
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = mode,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SaarathiNavy else TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = SaarathiTeal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Contacts Standby Info
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emergency Standby Roster",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${emergencyContacts.count { it.isActive }} priority contact(s) will receive instant SMS & live GPS coordinates if an unconfirmed high-G impact occurs.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onStartJourney,
                colors = ButtonDefaults.buttonColors(containerColor = SaarathiNavy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_start_journey_button")
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Arm Protection & Start Journey",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun JourneyActiveScreen(
    journey: Journey?,
    elapsedSeconds: Long,
    telemetry: SensorTelemetry,
    gpsStatus: GpsStatus,
    waypoints: List<LocationPoint>,
    latestDetection: DetectionResult?,
    mediumRiskAlert: MediumRiskAlert?,
    onDismissMediumAlert: (Boolean) -> Unit,
    onEndJourney: () -> Unit,
    onTriggerSimulation: (String) -> Unit,
    onManualSos: () -> Unit,
    onBack: () -> Unit
) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("journey_active_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SaarathiNavy)
            }
            Text(
                text = "Active Journey Tracking",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaarathiNavy
                )
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            // Reusable Mapbox Map Component
            MapboxMapView(
                userLatitude = telemetry.latitude ?: 12.9716,
                userLongitude = telemetry.longitude ?: 77.5946,
                headingDegrees = telemetry.headingDegrees,
                speedKmh = telemetry.speedKmh,
                waypoints = waypoints,
                showControls = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Non-Intrusive Medium Risk Alert (e.g. Sudden Braking or Rollover Anomaly)
            AnimatedVisibility(
                visible = mediumRiskAlert != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                mediumRiskAlert?.let { alert ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmberContainer),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningAmber),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .testTag("medium_risk_alert_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Possible incident detected.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = alert.reason,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            if (alert.signals.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                alert.signals.forEach { sig ->
                                    Text(
                                        text = "• $sig",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onDismissMediumAlert(false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("medium_alert_safe_button")
                                ) {
                                    Text("I'm Safe", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                OutlinedButton(
                                    onClick = { onDismissMediumAlert(true) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("medium_alert_continue_button")
                                ) {
                                    Text("Continue Monitoring", fontSize = 12.sp, color = SaarathiNavy)
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Journey & Kinematics Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaarathiNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "● LIVE PROTECTION",
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = telemetry.movementState.name,
                                color = SaarathiTealLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = timeFormatted,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Elapsed Time",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${telemetry.speedKmh.toInt()} km/h",
                                fontWeight = FontWeight.Bold,
                                color = SaarathiTeal,
                                fontSize = 17.sp
                            )
                            Text("GPS Speed", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.1f", telemetry.totalGForce)} G",
                                fontWeight = FontWeight.Bold,
                                color = SaarathiTeal,
                                fontSize = 17.sp
                            )
                            Text("Kinetic Load", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${telemetry.rotationRateDps.toInt()}°/s",
                                fontWeight = FontWeight.Bold,
                                color = SaarathiTeal,
                                fontSize = 17.sp
                            )
                            Text("Angular Rate", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hardware Telemetry & Signals Diagnostic Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sensors, contentDescription = null, tint = SaarathiTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Telemetry & Signal Extraction (~20Hz)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Accelerometer: X=${String.format("%.2f", telemetry.accelerometerX)}, Y=${String.format("%.2f", telemetry.accelerometerY)}, Z=${String.format("%.2f", telemetry.accelerometerZ)} m/s²\n" +
                                "• Gyroscope: Roll=${String.format("%.1f", telemetry.rotationRateDps)}°/s • Jerk=${String.format("%.1f", telemetry.jerkGPerSec)} G/s\n" +
                                "• GPS State: $gpsStatus • Waypoints Buffered: ${waypoints.size}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    latestDetection?.let { det ->
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BorderSubtle)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (det.isIncidentCandidate) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (det.isIncidentCandidate) WarningAmber else SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rule Assessment: ${det.riskLevel.name} (${(det.confidence * 100).toInt()}% conf)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SaarathiNavy
                            )
                        }
                        Text(
                            text = det.reason,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Testing & Simulation Hub (Covers all 5 scenarios from Prompt 2)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sensor Test & Verification Hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SaarathiNavy
                    )
                    Text(
                        text = "Test rule-based engine detection against all kinematic patterns:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Normal Cruising & Phone Fall (Disambiguated to Low)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTriggerSimulation("NORMAL_CRUISING") },
                            colors = ButtonDefaults.buttonColors(containerColor = SaarathiNavy.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_normal_button")
                        ) {
                            Text("Normal Cruising", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onTriggerSimulation("PHONE_FALL") },
                            colors = ButtonDefaults.buttonColors(containerColor = SaarathiTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_phone_drop_button")
                        ) {
                            Text("Phone Fall (Low)", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Sudden Braking & Rollover (Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTriggerSimulation("SUDDEN_BRAKING") },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_sudden_braking_button")
                        ) {
                            Text("Sudden Braking", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { onTriggerSimulation("ROLLOVER_ANOMALY") },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sim_rollover_button")
                        ) {
                            Text("Rollover (Med)", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: High-G Collision Crash (High -> 10s Countdown)
                    Button(
                        onClick = { onTriggerSimulation("HIGH_G_CRASH") },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("sim_high_crash_button")
                    ) {
                        Text("High-G Collision Crash (High Risk)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Manual SOS Button
            Button(
                onClick = onManualSos,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("active_journey_sos_button")
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MANUAL SOS — IMMEDIATE ESCALATION",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // End Journey Button
            OutlinedButton(
                onClick = onEndJourney,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("end_journey_button")
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = SaarathiNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "End Journey Safely",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SaarathiNavy
                )
            }
        }
    }
}
