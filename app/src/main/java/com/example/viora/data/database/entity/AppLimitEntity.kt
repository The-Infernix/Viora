package com.example.viora.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_limits")
data class AppLimitEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val interventionLevel: Int = 2,
    val isEnabled: Boolean = true,
    val customMessage: String? = null,
    val category: String
)
