package com.example.viora.domain.model

data class AppUsage(
    val packageName: String,
    val appName: String,
    val durationMinutes: Int,
    val sessionCount: Int = 0,
    val limitMinutes: Int? = null,
    val category: AppCategory = AppCategory.OTHER
)
