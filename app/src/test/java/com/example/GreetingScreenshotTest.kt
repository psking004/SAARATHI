package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.UserProfile
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.SaarathiTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SaarathiDashboardScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun saarathi_dashboard_screenshot() {
    val sampleUser = UserProfile(
      id = "user_1",
      fullName = "Vikram Sharma",
      email = "vikram@saarathi.org",
      phone = "+91 98450 12345",
      bloodType = "O+",
      medicalNotes = "No allergies."
    )

    composeTestRule.setContent {
      SaarathiTheme {
        DashboardScreen(
          userProfile = sampleUser,
          activeJourney = null,
          emergencyContacts = emptyList(),
          onStartJourneyClick = {},
          onActiveJourneyClick = {},
          onEmergencyContactsClick = {},
          onJourneyHistoryClick = {},
          onSafetyClick = {},
          onProfileClick = {},
          onManualSosTrigger = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/dashboard.png")
  }
}
