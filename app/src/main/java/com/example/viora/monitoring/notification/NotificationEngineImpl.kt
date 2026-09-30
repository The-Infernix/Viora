package com.example.viora.monitoring.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationEngineImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val factory: NotificationFactory
) : NotificationEngine {

    private val notificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun buildForegroundNotification(text: String): Notification {
        return factory.buildForegroundNotification(text)
    }

    override fun updateForegroundNotification(text: String) {
        notificationManager.notify(
            NotificationKind.FOREGROUND.id,
            factory.buildForegroundNotification(text)
        )
    }

    override fun showTrackingNotification(title: String, content: String) {
        notificationManager.notify(
            NotificationKind.TRACKING.id,
            factory.buildTrackingNotification(title, content)
        )
    }

    override fun showInterventionNotification(title: String, content: String) {
        notificationManager.notify(
            NotificationKind.INTERVENTION.id,
            factory.buildInterventionNotification(title, content)
        )
    }

    override fun cancel(kind: NotificationKind) {
        notificationManager.cancel(kind.id)
    }
}
