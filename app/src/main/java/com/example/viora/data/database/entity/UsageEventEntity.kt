package com.example.viora.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_events",
    indices = [
        Index("date"),
        Index("packageName"),
        Index("date", "hourOfDay")
    ]
)
data class UsageEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val appCategory: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val date: String,
    val hourOfDay: Int,
    val wasInterrupted: Boolean = false,
    val interventionLevel: Int = 0
)
