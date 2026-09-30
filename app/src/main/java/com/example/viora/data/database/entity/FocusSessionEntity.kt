package com.example.viora.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_sessions",
    indices = [Index("date")]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val startTime: Long,
    val endTime: Long? = null,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int? = null,
    val completed: Boolean = false,
    val interruptedCount: Int = 0,
    val ambientSound: String? = null,
    val date: String
)
