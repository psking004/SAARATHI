package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.UiEvent
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyContactsScreen
import com.example.ui.screens.EmergencyStateScreen
import com.example.ui.screens.GoodSamaritanScreen
import com.example.ui.screens.IncidentConfirmationScreen
import com.example.ui.screens.IncidentHistoryScreen
import com.example.ui.screens.JourneyActiveScreen
import com.example.ui.screens.JourneySetupScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ResponderDashboardScreen
import com.example.ui.screens.SafetyGuidanceScreen
import com.example.ui.screens.SafetyScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SaarathiBackground
import com.example.ui.theme.SaarathiNavy
import com.example.ui.theme.SaarathiTeal
import com.example.ui.theme.SaarathiTheme
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : BottomNavItem("dashboard", "HOME", Icons.Default.Home)
    data object Journey : BottomNavItem("journey_setup", "TRIP", Icons.Default.Navigation)
    data object Contacts : BottomNavItem("emergency_contacts", "CONTACTS", Icons.Default.Contacts)
    data object Safety : BottomNavItem("safety", "PROTECT", Icons.Default.Security)
    data object Profile : BottomNavItem("profile", "PROFILE", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaarathiTheme {
                SaarathiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SaarathiApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val session by viewModel.userSession.collectAsState()
    val isSessionLoading by viewModel.isSessionLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val activeJourney by viewModel.activeJourney.collectAsState()
    val activeIncident by viewModel.activeIncident.collectAsState()
    val countdownSeconds by viewModel.countdownSeconds.collectAsState()
    val journeyElapsedSeconds by viewModel.journeyElapsedSeconds.collectAsState()
    val telemetry by viewModel.latestTelemetry.collectAsState()
    val gpsStatus by viewModel.gpsStatus.collectAsState()
    val waypoints by viewModel.sampledWaypoints.collectAsState()
    val latestDetection by viewModel.latestDetection.collectAsState()
    val mediumRiskAlert by viewModel.mediumRiskAlert.collectAsState()
    val incidentHistory by viewModel.incidentHistory.collectAsState()
    val emergencyState by viewModel.emergencyState.collectAsState()
    val nearbyResponders by viewModel.nearbyResponders.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle ViewModel UI Events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Journey,
        BottomNavItem.Contacts,
        BottomNavItem.Safety,
        BottomNavItem.Profile
    )

    val showBottomBar = currentRoute in listOf(
        "dashboard",
        "emergency_contacts",
        "safety",
        "profile"
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = BorderSubtle,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (item.route == "journey_setup" && activeJourney != null) {
                                    navController.navigate("journey_active") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) SaarathiNavy else TextSecondary.copy(alpha = 0.65f)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp,
                                    color = if (isSelected) SaarathiNavy else TextSecondary.copy(alpha = 0.65f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SaarathiNavy.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        },
        containerColor = SaarathiBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(
                    onSessionChecked = {
                        navController.navigate("dashboard") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    },
                    onLoginClick = { email, pass, onSuccess, onError ->
                        viewModel.login(email, pass, onSuccess, onError)
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterClick = { name, email, phone, blood, medical, pass, confirm, onSuccess, onError ->
                        viewModel.register(name, email, phone, blood, medical, pass, confirm, onSuccess, onError)
                    }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    userProfile = userProfile,
                    activeJourney = activeJourney,
                    emergencyContacts = emergencyContacts,
                    onStartJourneyClick = {
                        navController.navigate("journey_setup")
                    },
                    onActiveJourneyClick = {
                        navController.navigate("journey_active")
                    },
                    onEmergencyContactsClick = {
                        navController.navigate("emergency_contacts")
                    },
                    onJourneyHistoryClick = {
                        navController.navigate("incident_history")
                    },
                    onSafetyClick = {
                        navController.navigate("safety")
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    },
                    onManualSosTrigger = {
                        viewModel.triggerManualSOS()
                    }
                )
            }

            composable("journey_setup") {
                JourneySetupScreen(
                    emergencyContacts = emergencyContacts,
                    onStartJourney = {
                        viewModel.startJourney {
                            navController.navigate("journey_active") {
                                popUpTo("journey_setup") { inclusive = true }
                            }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("journey_active") {
                JourneyActiveScreen(
                    journey = activeJourney,
                    elapsedSeconds = journeyElapsedSeconds,
                    telemetry = telemetry,
                    gpsStatus = gpsStatus,
                    waypoints = waypoints,
                    latestDetection = latestDetection,
                    mediumRiskAlert = mediumRiskAlert,
                    onDismissMediumAlert = { continueMonitoring ->
                        viewModel.dismissMediumRiskAlert(continueMonitoring)
                    },
                    onEndJourney = {
                        viewModel.endJourney {
                            navController.navigate("dashboard") {
                                popUpTo("journey_active") { inclusive = true }
                            }
                        }
                    },
                    onTriggerSimulation = { testCase ->
                        viewModel.triggerSimulation(testCase)
                    },
                    onManualSos = {
                        viewModel.triggerManualSOS()
                    },
                    onBack = {
                        navController.navigate("dashboard")
                    }
                )
            }

            composable("incident_confirmation") {
                IncidentConfirmationScreen(
                    incident = activeIncident,
                    countdownSeconds = countdownSeconds,
                    onImOkayClick = {
                        viewModel.dismissIncidentAsFalseAlarm {
                            navController.navigate("dashboard") {
                                popUpTo("incident_confirmation") { inclusive = true }
                            }
                        }
                    },
                    onNeedHelpClick = {
                        viewModel.confirmIncidentImmediately {
                            navController.navigate("emergency_state") {
                                popUpTo("incident_confirmation") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("emergency_state") {
                EmergencyStateScreen(
                    incident = activeIncident,
                    userProfile = userProfile,
                    emergencyContacts = emergencyContacts,
                    emergencyState = emergencyState,
                    nearbyResponders = nearbyResponders,
                    onNavigateToSafetyGuidance = {
                        navController.navigate("safety_guidance")
                    },
                    onNavigateToGoodSamaritan = {
                        navController.navigate("good_samaritan")
                    },
                    onResolveEmergency = {
                        viewModel.resolveEmergency {
                            navController.navigate("dashboard") {
                                popUpTo("emergency_state") { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable("emergency_contacts") {
                EmergencyContactsScreen(
                    contacts = emergencyContacts,
                    onSaveContact = { id, name, phone, rel, priority, active ->
                        viewModel.saveContact(id, name, phone, rel, priority, active) {}
                    },
                    onDeleteContact = { id ->
                        viewModel.deleteContact(id)
                    },
                    onToggleActive = { contact ->
                        viewModel.toggleContactActive(contact)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("profile") {
                ProfileScreen(
                    userProfile = userProfile,
                    onUpdateProfile = { name, phone, blood, notes ->
                        viewModel.updateProfile(name, phone, blood, notes) {}
                    },
                    onLogout = {
                        viewModel.logout {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("incident_history") {
                IncidentHistoryScreen(
                    incidents = incidentHistory,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("safety") {
                SafetyScreen(
                    onNavigateToGuidance = {
                        navController.navigate("safety_guidance")
                    },
                    onNavigateToGoodSamaritan = {
                        navController.navigate("good_samaritan")
                    },
                    onNavigateToResponder = {
                        navController.navigate("responder_dashboard")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("responder_dashboard") {
                ResponderDashboardScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToGoodSamaritan = {
                        navController.navigate("good_samaritan")
                    },
                    onNavigateToSafetyGuidance = {
                        navController.navigate("safety_guidance")
                    }
                )
            }

            composable("safety_guidance") {
                SafetyGuidanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToGoodSamaritan = {
                        navController.navigate("good_samaritan")
                    }
                )
            }

            composable("good_samaritan") {
                GoodSamaritanScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
