package com.example.viora.monitoring.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        val trackingChannel = NotificationChannel(
            CHANNEL_TRACKING,
            "Usage Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when Viora is monitoring usage"
            setShowBadge(false)
        }

        val interventionChannel = NotificationChannel(
            CHANNEL_INTERVENTION,
            "Interventions",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows when you need a break"
        }

        val focusChannel = NotificationChannel(
            CHANNEL_FOCUS,
            "Focus Mode",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows focus session timer"
            setSound(null, null)
        }

        notificationManager.createNotificationChannels(
            listOf(trackingChannel, interventionChannel, focusChannel)
        )
    }

    fun buildForegroundNotification(text: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_TRACKING)
            .setContentTitle("Viora")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun buildTrackingNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_TRACKING)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun buildInterventionNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_INTERVENTION)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    companion object {
        const val CHANNEL_TRACKING = "tracking_channel"
        const val CHANNEL_INTERVENTION = "intervention_channel"
        const val CHANNEL_FOCUS = "focus_channel"
    }
}
