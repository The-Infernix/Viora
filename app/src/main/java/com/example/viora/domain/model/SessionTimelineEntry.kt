package com.example.viora.domain.model

data class SessionTimelineEntry(
    val packageName: String,
    val appName: String,
    val startHour: Int,
    val durationMinutes: Int,
    val category: AppCategory
)
