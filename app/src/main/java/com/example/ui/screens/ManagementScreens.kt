package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.EmergencyContact
import com.example.domain.model.Incident
import com.example.domain.model.UserProfile
import com.example.ui.components.QuickEmergencyCallCard
import com.example.ui.components.RiskBadge
import com.example.ui.components.SaarathiHeader
import com.example.ui.components.StatusBadge
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
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// 1. EMERGENCY CONTACTS SCREEN
// ==========================================
@Composable
fun EmergencyContactsScreen(
    contacts: List<EmergencyContact>,
    onSaveContact: (id: String?, name: String, phone: String, rel: String, priority: Int, active: Boolean) -> Unit,
    onDeleteContact: (id: String) -> Unit,
    onToggleActive: (EmergencyContact) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }

    fun dialNumber(number: String) {
        val cleanNumber = number.replace(" ", "").replace("-", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        context.startActivity(intent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .testTag("emergency_contacts_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = "Emergency Contacts",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SaarathiNavy
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Contacts Added Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add family and close guardians who should receive automated alerts during the Golden Hour.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = SaarathiTealLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = SaarathiTeal)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Contacts are alerted in order of priority (1, 2, 3) with your live GPS location during emergencies.",
                                    fontSize = 12.sp,
                                    color = SaarathiNavy,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    items(contacts, key = { it.id }) { contact ->
                        ContactItemCard(
                            contact = contact,
                            onDial = { dialNumber(contact.phone) },
                            onEdit = {
                                editingContact = contact
                                showAddDialog = true
                            },
                            onDelete = { onDeleteContact(contact.id) },
                            onToggleActive = { onToggleActive(contact) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Floating Action Button to Add Contact
        FloatingActionButton(
            onClick = {
                editingContact = null
                showAddDialog = true
            },
            containerColor = SaarathiNavy,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_contact_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }

        if (showAddDialog) {
            ContactDialog(
                initial = editingContact,
                onDismiss = {
                    showAddDialog = false
                    editingContact = null
                },
                onSave = { name, phone, rel, priority, active ->
                    onSaveContact(editingContact?.id, name, phone, rel, priority, active)
                    showAddDialog = false
                    editingContact = null
                }
            )
        }
    }
}

@Composable
fun ContactItemCard(
    contact: EmergencyContact,
    onDial: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (contact.isActive) SaarathiTeal.copy(alpha = 0.15f) else BorderSubtle),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P${contact.priority}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (contact.isActive) SaarathiTeal else TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = SaarathiBackground,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = contact.relationship,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.phone,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onDial) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = SaarathiTeal)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = EmergencyRed)
            }
        }
    }
}

@Composable
fun ContactDialog(
    initial: EmergencyContact?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, rel: String, priority: Int, active: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var relationship by remember { mutableStateOf(initial?.relationship ?: "Family") }
    var priority by remember { mutableIntStateOf(initial?.priority ?: 1) }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initial == null) "Add Emergency Contact" else "Edit Contact",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_phone_input")
                )
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship (e.g., Parent, Spouse)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_rel_input")
                )
                Text("Priority Level (1 is highest)", fontSize = 12.sp, color = TextSecondary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1, 2, 3).forEach { p ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (priority == p) SaarathiNavy else SaarathiTealLight,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p }
                        ) {
                            Text(
                                text = "Priority $p",
                                textAlign = TextAlign.Center,
                                color = if (priority == p) Color.White else SaarathiNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name, phone, relationship, priority, isActive)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaarathiNavy),
                modifier = Modifier.testTag("save_contact_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// 2. PROFILE & SETTINGS SCREEN
// ==========================================
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onUpdateProfile: (name: String, phone: String, blood: String, notes: String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("profile_screen")
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
                text = "Emergency Medical Profile",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaarathiNavy
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Profile Card Header
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaarathiNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SaarathiTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = userProfile?.fullName ?: "Saarathi Guardian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = userProfile?.email ?: "",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Triage Details
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Medical Triage Data",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = SaarathiTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileFieldRow(label = "Emergency Phone", value = userProfile?.phone?.ifBlank { "Not set" } ?: "Not set")
                    ProfileFieldRow(label = "Blood Group", value = userProfile?.bloodType?.ifBlank { "O+" } ?: "O+")
                    ProfileFieldRow(
                        label = "Medical Notes & Allergies",
                        value = userProfile?.medicalNotes?.ifBlank { "None reported. NKDA." } ?: "None reported."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account & Session Security Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Session & Authentication",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Session token stored securely with local persistence and Supabase Auth backend synchronization.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Out Button
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Text(
                    text = "Sign Out of SAARATHI",
                    color = EmergencyRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                current = userProfile,
                onDismiss = { showEditDialog = false },
                onSave = { name, phone, blood, notes ->
                    onUpdateProfile(name, phone, blood, notes)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileFieldRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun EditProfileDialog(
    current: UserProfile?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, blood: String, notes: String) -> Unit
) {
    var fullName by remember { mutableStateOf(current?.fullName ?: "") }
    var phone by remember { mutableStateOf(current?.phone ?: "") }
    var bloodType by remember { mutableStateOf(current?.bloodType ?: "O+") }
    var medicalNotes by remember { mutableStateOf(current?.medicalNotes ?: "") }

    val bloodOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Emergency Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Emergency Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Blood Group", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bloodOptions.take(4).forEach { b ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (bloodType == b) SaarathiNavy else SaarathiTealLight,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { bloodType = b }
                        ) {
                            Text(
                                text = b,
                                textAlign = TextAlign.Center,
                                color = if (bloodType == b) Color.White else SaarathiNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    bloodOptions.takeLast(4).forEach { b ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (bloodType == b) SaarathiNavy else SaarathiTealLight,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { bloodType = b }
                        ) {
                            Text(
                                text = b,
                                textAlign = TextAlign.Center,
                                color = if (bloodType == b) Color.White else SaarathiNavy,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = medicalNotes,
                    onValueChange = { medicalNotes = it },
                    label = { Text("Medical Notes (Allergies, Medications)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(fullName, phone, bloodType, medicalNotes) },
                colors = ButtonDefaults.buttonColors(containerColor = SaarathiNavy)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ==========================================
// 3. INCIDENT HISTORY SCREEN
// ==========================================
@Composable
fun IncidentHistoryScreen(
    incidents: List<Incident>,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .testTag("incident_history_screen")
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
                text = "Incident & Event History",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaarathiNavy
                ),
                modifier = Modifier.weight(1f)
            )
        }

        if (incidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Incident Records",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All your previous journeys were safe with zero detected incidents.",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(incidents, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(item.status)
                                RiskBadge(item.riskLevel)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.notes.ifBlank { "Automated detection record" },
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detected: ${dateFormat.format(Date(item.detectedAt))}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            if (item.latitude != null && item.longitude != null) {
                                Text(
                                    text = "Coordinates: ${String.format("%.4f", item.latitude)}, ${String.format("%.4f", item.longitude)}",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// ==========================================
// 4. SAFETY & GOLDEN HOUR DIRECTORY SCREEN
// ==========================================
@Composable
fun SafetyScreen(
    onNavigateToGuidance: () -> Unit = {},
    onNavigateToGoodSamaritan: () -> Unit = {},
    onNavigateToResponder: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current

    fun dial(num: String) {
        val cleanNumber = num.replace(" ", "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaarathiBackground)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
            .testTag("safety_screen")
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
                text = "Golden Hour & Safety Hub",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SaarathiNavy
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // The Golden Hour Principle Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SaarathiNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        color = WarningAmberContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "WHAT IS THE GOLDEN HOUR?",
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The first 60 minutes following severe traumatic injury determine survival probability.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Prompt medical care during this window prevents irreversible hemorrhage, hypoxia, and shock. SAARATHI eliminates critical delay by orchestrating bystanders, priority contacts, and 112 emergency services instantly.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Interactive Tools Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SaarathiTealLight),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGuidance() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = SaarathiTeal,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "First-Aid Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SaarathiNavy
                        )
                        Text(
                            text = "Multi-language audio instructions",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGoodSamaritan() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Good Samaritan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Legal protection under §134A",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Responder Program Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToResponder() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SaarathiBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = SaarathiBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Community Responder Network",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Register as a certified first-aid volunteer to assist nearby emergencies.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // National Emergency Directory
            Text(
                text = "National Emergency Helplines",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            QuickEmergencyCallCard("National Unified Emergency", "112", "All Emergency Services", { dial("112") })
            Spacer(modifier = Modifier.height(8.dp))
            QuickEmergencyCallCard("Medical Emergency / Ambulance", "108", "Emergency Trauma & ICU Ambulance", { dial("108") })
            Spacer(modifier = Modifier.height(8.dp))
            QuickEmergencyCallCard("Police Helpline", "100", "Immediate Police Assistance", { dial("100") })
            Spacer(modifier = Modifier.height(8.dp))
            QuickEmergencyCallCard("Fire & Rescue", "101", "Fire Hazard & Extraction Units", { dial("101") })

            Spacer(modifier = Modifier.height(24.dp))

            // First Responder Essential Guides
            Text(
                text = "First Responder Action Protocols",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            SafetyGuideCard(
                title = "1. Severe Bleeding Control",
                description = "Apply firm, continuous direct pressure with a clean cloth or tourniquet above the wound if limb bleeding is arterial (spurting). Do not release pressure."
            )
            Spacer(modifier = Modifier.height(8.dp))
            SafetyGuideCard(
                title = "2. Helmet & Spinal Precautions",
                description = "DO NOT remove a motorcyclist's helmet unless their airway is blocked or they are not breathing. Stabilize head and neck in a neutral in-line position."
            )
            Spacer(modifier = Modifier.height(8.dp))
            SafetyGuideCard(
                title = "3. Shock Management",
                description = "Keep victim warm with a jacket or blanket. Reassure them gently. Do not give any food, water, or liquids by mouth as surgery may be required."
            )
        }
    }
}

@Composable
fun SafetyGuideCard(title: String, description: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SaarathiNavy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
