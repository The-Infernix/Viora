package com.example.viora.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val date: String,
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
