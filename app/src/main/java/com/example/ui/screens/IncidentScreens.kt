package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyContact
import com.example.domain.model.EmergencyState
import com.example.domain.model.Incident
import com.example.domain.model.ResponderProfile
import com.example.domain.model.RiskLevel
import com.example.domain.model.UserProfile
import com.example.ui.components.MapboxMapView
import com.example.ui.components.QuickEmergencyCallCard
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.SaarathiBackground
import com.example.ui.theme.SaarathiBlue
import com.example.ui.theme.SaarathiNavy
import com.example.ui.theme.SaarathiTeal
import com.example.ui.theme.SaarathiTealDark
import com.example.ui.theme.SaarathiTealLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 10-Second High-Risk Incident Confirmation Screen.
 * Prompt 2 Spec:
 * - Countdown: 10 seconds
 * - Title: "Possible crash detected."
 * - Buttons: "I'M SAFE" and "I NEED HELP NOW"
 * - Auto-escalation message on timeout: "Emergency response initiated based on high-risk detection and no response."
 */
@Composable
fun IncidentConfirmationScreen(
    incident: Incident?,
    countdownSeconds: Int,
    onImOkayClick: () -> Unit,
    onNeedHelpClick: () -> Unit
) {
    val progress = countdownSeconds / 10f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("incident_confirmation_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // High Risk Warning Badge
        Surface(
            color = EmergencyRedContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = EmergencyRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CRITICAL KINETIC IMPACT DETECTED",
                    color = EmergencyRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 10-Second Countdown Progress Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = EmergencyRed,
                strokeWidth = 8.dp,
                trackColor = EmergencyRedContainer
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$countdownSeconds",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmergencyRed
                )
                Text(
                    text = "SECONDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Possible crash detected.",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = SaarathiNavy
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Severe impact force and rapid deceleration detected. If you do not respond, emergency protocol will automatically initiate to secure your Golden Hour.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Button 1: "I'M SAFE" (Cancels alert, marks false alarm/dismissed)
        Button(
            onClick = onImOkayClick,
            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("im_okay_button")
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "I'M SAFE — DISMISS ALERT",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Button 2: "I NEED HELP NOW" (Immediately triggers emergency state)
        Button(
            onClick = onNeedHelpClick,
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("need_help_button")
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "I NEED HELP NOW",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Emergency State Screen (Priority Hierarchy):
 * 1. State Machine Progress & Status
 * 2. Situation Summary
 * 3. User Actions (Instant Dials 112/108 & Contacts)
 * 4. Location (Mapbox map preview, exact coordinates)
 * 5. Nearby First Responders Coordination
 * 6. Safety Guidance & First-Aid Access
 * 7. Bystander Protection (Good Samaritan Act)
 */
@Composable
fun EmergencyStateScreen(
    incident: Incident?,
    userProfile: UserProfile?,
    emergencyContacts: List<EmergencyContact>,
    emergencyState: EmergencyState = EmergencyState.EMERGENCY_ACTIVE,
    nearbyResponders: List<Pair<ResponderProfile, Double>> = emptyList(),
    onNavigateToSafetyGuidance: () -> Unit = {},
    onNavigateToGoodSamaritan: () -> Unit = {},
    onResolveEmergency: () -> Unit
) {
    val context = LocalContext.current

    fun dialNumber(number: String) {
        val cleanNumber = number.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        context.startActivity(intent)
    }

    val lat = incident?.latitude ?: 12.9716
    val lng = incident?.longitude ?: 77.5946
    val detectedDate = SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.getDefault()).format(Date(incident?.detectedAt ?: System.currentTimeMillis()))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("emergency_state_screen")
    ) {
        // Red Emergency Alert Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmergencyRed)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY STATE ACTIVE",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Golden Hour Protocol Initiated • Stay Calm",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // State Machine Transition Indicator
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "State: ${emergencyState.name.replace("_", " ")}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (emergencyState) {
                                EmergencyState.HELP_REQUESTED -> "Broadcasting alerts to emergency contacts & qualified volunteers..."
                                EmergencyState.RESPONDER_FOUND -> "Nearby community responder notified and en-route."
                                EmergencyState.PROFESSIONAL_HELP_PENDING -> "Call 112/108 below for direct ambulance dispatch."
                                else -> "Emergency coordination active. Follow on-screen safety steps."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------------------------------------------
            // 1. SITUATION SUMMARY
            // -------------------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Situation Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        RiskBadge(incident?.riskLevel ?: RiskLevel.HIGH)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = incident?.notes?.ifBlank { "Emergency response initiated based on high-risk detection and no response." }
                            ?: "Emergency response initiated based on high-risk detection and no response.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Time: $detectedDate",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Status: ${incident?.status?.name ?: "CONFIRMED"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EmergencyRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------------------------------------------
            // 2. USER ACTIONS (Instant Direct Dials)
            // -------------------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. User Action (Instant Dials)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Surface(
                            color = EmergencyRedContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "TAP TO DIAL",
                                color = EmergencyRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    QuickEmergencyCallCard(
                        title = "National Emergency (112)",
                        phoneNumber = "112",
                        label = "Police, Fire & Unified Emergency Response",
                        onCallClick = { dialNumber("112") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickEmergencyCallCard(
                        title = "Medical Ambulance (108)",
                        phoneNumber = "108",
                        label = "Direct Trauma Response Unit",
                        onCallClick = { dialNumber("108") }
                    )

                    if (emergencyContacts.isNotEmpty()) {
                        val primary = emergencyContacts.firstOrNull { it.isActive } ?: emergencyContacts.first()
                        Spacer(modifier = Modifier.height(8.dp))
                        QuickEmergencyCallCard(
                            title = primary.name,
                            phoneNumber = primary.phone,
                            label = "Primary Contact (${primary.relationship})",
                            onCallClick = { dialNumber(primary.phone) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------------------------------------------
            // 3. LOCATION WITH MAPBOX MAP
            // -------------------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SaarathiBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Incident Location & Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Mapbox Map with Incident Marker
                    MapboxMapView(
                        userLatitude = lat,
                        userLongitude = lng,
                        incidentLatitude = lat,
                        incidentLongitude = lng,
                        showControls = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "GPS Coordinates: ${String.format("%.5f", lat)}° N, ${String.format("%.5f", lng)}° E",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SaarathiNavy
                    )
                    Text(
                        text = "Live location broadcast to emergency contacts and nearby responder network.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------------------------------------------
            // 4. NEARBY RESPONDERS IN VICINITY
            // -------------------------------------------------------------------------------------
            if (nearbyResponders.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "4. Nearby Volunteer Responders",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreenContainer
                            ) {
                                Text(
                                    text = "${nearbyResponders.size} NEARBY",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        nearbyResponders.take(2).forEach { (responder, distance) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = responder.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${responder.type.name.replace("_", " ")} • ~${String.format("%.1f", distance / 1000)} km away",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Button(
                                    onClick = { dialNumber(responder.phone) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // -------------------------------------------------------------------------------------
            // 5. SAFETY GUIDANCE & FIRST AID LINK
            // -------------------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SaarathiTealLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSafetyGuidance() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = SaarathiTealDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Safety & First-Aid Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SaarathiNavy
                        )
                        Text(
                            text = "Step-by-step CPR, bleeding control & voice guidance in English, Hindi, Kannada, Tamil.",
                            fontSize = 12.sp,
                            color = SaarathiNavy.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // -------------------------------------------------------------------------------------
            // 6. GOOD SAMARITAN LEGAL SHIELD
            // -------------------------------------------------------------------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGoodSamaritan() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Good Samaritan Legal Shield (§134A)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Zero civil or criminal liability for assisting crash victims in good faith.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Safe Resolution Button
            Button(
                onClick = onResolveEmergency,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("resolve_emergency_button")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I Am Safe / Resolve Emergency",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

