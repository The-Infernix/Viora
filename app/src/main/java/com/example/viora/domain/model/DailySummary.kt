package com.example.viora.domain.model

data class DailySummary(
    val date: String,
    val totalScreenTimeMinutes: Int,
    val totalPickups: Int,
    val totalUnlocks: Int,
    val wellbeingScore: Int,
    val focusMinutes: Int,
    val focusSessionsCount: Int,
    val mostUsedApp: String?,
    val mostUsedAppMinutes: Int,
    val peakHour: Int,
    val interventionsTriggered: Int,
    val dailyGoalMinutes: Int,
    val goalMet: Boolean,
    val streakDay: Boolean,
    val calculatedAt: Long
)
