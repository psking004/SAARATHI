package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyContact
import com.example.domain.model.Journey
import com.example.domain.model.UserProfile
import com.example.ui.components.SaarathiHeader
import com.example.ui.components.SafetyStatusBanner
import com.example.ui.components.SafetyTipBanner
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.SaarathiBackground
import com.example.ui.theme.SaarathiBlue
import com.example.ui.theme.SaarathiNavy
import com.example.ui.theme.SaarathiTeal
import com.example.ui.theme.SaarathiTealLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    userProfile: UserProfile?,
    activeJourney: Journey?,
    emergencyContacts: List<EmergencyContact>,
    onStartJourneyClick: () -> Unit,
    onActiveJourneyClick: () -> Unit,
    onEmergencyContactsClick: () -> Unit,
    onJourneyHistoryClick: () -> Unit,
    onSafetyClick: () -> Unit,
    onProfileClick: () -> Unit,
    onManualSosTrigger: () -> Unit
) {
    val userInitials = userProfile?.fullName
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.mapNotNull { it.firstOrNull()?.uppercase() }
        ?.take(2)
        ?.joinToString("")
        ?.ifBlank { "JD" } ?: "JD"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("dashboard_screen")
    ) {
        // Top Minimalist Header
        SaarathiHeader(
            title = "SAARATHI",
            kicker = "Dashboard",
            initials = userInitials,
            onAvatarClick = onProfileClick
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Safety Status Card
            SafetyStatusBanner()

            // Primary CTA: Start Journey (or Active Journey status)
            if (activeJourney != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SaarathiTealLight),
                    border = BorderStroke(1.dp, SaarathiTeal.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActiveJourneyClick() }
                        .testTag("active_journey_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SaarathiTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Journey in Progress",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaarathiNavy
                            )
                            Text(
                                text = "Accident detection armed",
                                fontSize = 12.sp,
                                color = SaarathiNavy.copy(alpha = 0.75f)
                            )
                        }
                        Button(
                            onClick = onActiveJourneyClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SaarathiNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // Large Minimal Navy Primary CTA
                Button(
                    onClick = onStartJourneyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SaarathiNavy),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .testTag("start_journey_primary_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "START JOURNEY",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Secondary Actions Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val activeContactsCount = emergencyContacts.count { it.isActive }
                DashboardActionCard(
                    title = "Contacts",
                    subtitle = "$activeContactsCount Active",
                    icon = Icons.Default.Contacts,
                    iconTint = SaarathiBlue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_contacts_card"),
                    onClick = onEmergencyContactsClick
                )

                DashboardActionCard(
                    title = "History",
                    subtitle = "Past Records",
                    icon = Icons.Default.History,
                    iconTint = WarningAmber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_history_card"),
                    onClick = onJourneyHistoryClick
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DashboardActionCard(
                    title = "Safety Hub",
                    subtitle = "Golden Hour",
                    icon = Icons.Default.Security,
                    iconTint = SaarathiTeal,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_safety_card"),
                    onClick = onSafetyClick
                )

                DashboardActionCard(
                    title = "Medical ID",
                    subtitle = userProfile?.bloodType?.ifBlank { "O+" } ?: "Triage Profile",
                    icon = Icons.Default.Favorite,
                    iconTint = SaarathiNavy,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_profile_card"),
                    onClick = onProfileClick
                )
            }

            // Safety Tip Banner
            SafetyTipBanner(
                tipText = "Always ensure your phone is secured in a mount before starting a drive."
            )

            // Manual Emergency SOS Trigger Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmergencyRedContainer),
                border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
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
                            text = "Manual Emergency SOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EmergencyRed
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Instantly coordinates emergency dispatch (112) and alerts your priority contacts with live coordinates.",
                        fontSize = 12.sp,
                        color = TextPrimary.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onManualSosTrigger,
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("manual_sos_button")
                    ) {
                        Text(
                            text = "TRIGGER EMERGENCY SOS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}
