package com.example.viora.mindful.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unlock_events",
    indices = [
        Index("date"),
        Index("selectedReasonId"),
        Index("date", "hourOfDay")
    ]
)
data class UnlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val date: String,
    val hourOfDay: Int,
    val selectedReasonId: String,
    val selectedReasonLabel: String,
    val wasJustChecking: Boolean,
    val lockedPhoneAgain: Boolean,
    val sessionLengthSeconds: Int,
    val openedSocialMediaAfter: Boolean,
    val promptShown: Boolean,
    val promptSkipped: Boolean
)
