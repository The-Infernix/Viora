package com.example.viora.monitoring.notification

import android.app.Notification

enum class NotificationKind(val id: Int) {
    FOREGROUND(100),
    TRACKING(1),
    INTERVENTION(2),
    FOCUS(3)
}

interface NotificationEngine {
    fun buildForegroundNotification(text: String): Notification
    fun updateForegroundNotification(text: String)
    fun showTrackingNotification(title: String, content: String)
    fun showInterventionNotification(title: String, content: String)
    fun cancel(kind: NotificationKind)
}
