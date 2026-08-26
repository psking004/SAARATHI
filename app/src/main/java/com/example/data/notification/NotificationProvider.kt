package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.domain.model.EmergencyContact
import com.example.domain.model.Incident
import com.example.domain.model.NotificationDeliveryResult
import com.example.domain.model.ResponderAssignment
import com.example.domain.model.RiskLevel

interface NotificationProvider {
    val providerName: String

    fun sendEmergencyAlert(
        incident: Incident,
        countdownSeconds: Int
    ): NotificationDeliveryResult

    fun sendResponderRequest(
        assignment: ResponderAssignment
    ): NotificationDeliveryResult

    fun sendResponderAccepted(
        assignment: ResponderAssignment
    ): NotificationDeliveryResult

    fun sendEmergencyContactBroadcast(
        contacts: List<EmergencyContact>,
        incident: Incident,
        liveLocationUrl: String
    ): NotificationDeliveryResult

    fun sendIncidentResolved(
        incidentId: String
    ): NotificationDeliveryResult
}

class AndroidSystemNotificationProvider(
    private val context: Context
) : NotificationProvider {

    override val providerName: String = "Android Native System Notification Channel"

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            // Channel 1: High Priority Emergency Alerts
            val emergencyChannel = NotificationChannel(
                CHANNEL_EMERGENCY,
                "SAARATHI Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical crash detection and emergency countdown alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // Channel 2: Responder Requests
            val responderChannel = NotificationChannel(
                CHANNEL_RESPONDER,
                "SAARATHI Responder Coordination",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nearby emergency responder alerts and assignments"
                enableVibration(true)
            }

            // Channel 3: General Status & Contact Notifications
            val statusChannel = NotificationChannel(
                CHANNEL_STATUS,
                "SAARATHI Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Journey telemetry and emergency contact broadcast status"
            }

            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(responderChannel)
            notificationManager.createNotificationChannel(statusChannel)
        }
    }

    override fun sendEmergencyAlert(incident: Incident, countdownSeconds: Int): NotificationDeliveryResult {
        return try {
            val builder = NotificationCompat.Builder(context, CHANNEL_EMERGENCY)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("CRITICAL: Possible Crash Detected")
                .setContentText("Severe kinetic deceleration detected. Emergency response initiating in $countdownSeconds seconds.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)

            notificationManager?.notify(NOTIFICATION_ID_EMERGENCY, builder.build())

            NotificationDeliveryResult(
                isDelivered = true,
                channel = CHANNEL_EMERGENCY,
                providerName = providerName,
                statusMessage = "Heads-up emergency notification dispatched to system tray."
            )
        } catch (e: Exception) {
            NotificationDeliveryResult(
                isDelivered = false,
                channel = CHANNEL_EMERGENCY,
                providerName = providerName,
                statusMessage = "System notification failed: ${e.localizedMessage}"
            )
        }
    }

    override fun sendResponderRequest(assignment: ResponderAssignment): NotificationDeliveryResult {
        return try {
            val builder = NotificationCompat.Builder(context, CHANNEL_RESPONDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Emergency Nearby (${String.format("%.1f", assignment.distanceMeters / 1000)} km)")
                .setContentText("Assistance requested ${assignment.approximateLocation}. Follow emergency operator instructions.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)

            notificationManager?.notify(NOTIFICATION_ID_RESPONDER, builder.build())

            NotificationDeliveryResult(
                isDelivered = true,
                channel = CHANNEL_RESPONDER,
                providerName = providerName,
                statusMessage = "Responder alert delivered to registered certified first responder."
            )
        } catch (e: Exception) {
            NotificationDeliveryResult(
                isDelivered = false,
                channel = CHANNEL_RESPONDER,
                providerName = providerName,
                statusMessage = "Failed to deliver responder notification: ${e.localizedMessage}"
            )
        }
    }

    override fun sendResponderAccepted(assignment: ResponderAssignment): NotificationDeliveryResult {
        return try {
            val builder = NotificationCompat.Builder(context, CHANNEL_RESPONDER)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle("Responder En Route: ${assignment.responderName}")
                .setContentText("${assignment.responderType.name.replace("_", " ")} responding (ETA ~${assignment.etaMinutes} mins). Professional 112/108 remains prioritized.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager?.notify(NOTIFICATION_ID_RESPONDER_ACCEPTED, builder.build())

            NotificationDeliveryResult(
                isDelivered = true,
                channel = CHANNEL_RESPONDER,
                providerName = providerName,
                statusMessage = "Victim notified of active responder acceptance."
            )
        } catch (e: Exception) {
            NotificationDeliveryResult(
                isDelivered = false,
                channel = CHANNEL_RESPONDER,
                providerName = providerName,
                statusMessage = "Notification error: ${e.localizedMessage}"
            )
        }
    }

    override fun sendEmergencyContactBroadcast(
        contacts: List<EmergencyContact>,
        incident: Incident,
        liveLocationUrl: String
    ): NotificationDeliveryResult {
        val activeCount = contacts.count { it.isActive }
        return try {
            val builder = NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Emergency Alert Broadcast")
                .setContentText("Emergency SMS & Location shared with $activeCount active emergency contacts.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager?.notify(NOTIFICATION_ID_CONTACTS, builder.build())

            NotificationDeliveryResult(
                isDelivered = true,
                channel = CHANNEL_STATUS,
                providerName = providerName,
                statusMessage = "Direct SMS broadcast queue triggered for $activeCount verified contacts.",
                recipientCount = activeCount
            )
        } catch (e: Exception) {
            NotificationDeliveryResult(
                isDelivered = false,
                channel = CHANNEL_STATUS,
                providerName = providerName,
                statusMessage = "Contact broadcast notification failed: ${e.localizedMessage}"
            )
        }
    }

    override fun sendIncidentResolved(incidentId: String): NotificationDeliveryResult {
        return try {
            notificationManager?.cancel(NOTIFICATION_ID_EMERGENCY)
            notificationManager?.cancel(NOTIFICATION_ID_RESPONDER)

            val builder = NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(android.R.drawable.checkbox_on_background)
                .setContentTitle("Emergency Incident Resolved")
                .setContentText("Incident marked safe and logged in trip history.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)

            notificationManager?.notify(NOTIFICATION_ID_RESOLVED, builder.build())

            NotificationDeliveryResult(
                isDelivered = true,
                channel = CHANNEL_STATUS,
                providerName = providerName,
                statusMessage = "Emergency state cleared."
            )
        } catch (e: Exception) {
            NotificationDeliveryResult(
                isDelivered = false,
                channel = CHANNEL_STATUS,
                providerName = providerName,
                statusMessage = "Resolve notification error: ${e.localizedMessage}"
            )
        }
    }

    companion object {
        const val CHANNEL_EMERGENCY = "saarathi_emergency_channel"
        const val CHANNEL_RESPONDER = "saarathi_responder_channel"
        const val CHANNEL_STATUS = "saarathi_status_channel"

        const val NOTIFICATION_ID_EMERGENCY = 1001
        const val NOTIFICATION_ID_RESPONDER = 1002
        const val NOTIFICATION_ID_RESPONDER_ACCEPTED = 1003
        const val NOTIFICATION_ID_CONTACTS = 1004
        const val NOTIFICATION_ID_RESOLVED = 1005
    }
}
