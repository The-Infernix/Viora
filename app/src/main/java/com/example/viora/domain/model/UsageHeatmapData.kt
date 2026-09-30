package com.example.viora.domain.model

data class UsageHeatmapData(
    val dayOfWeek: Int,  // 0 = Monday, 6 = Sunday
    val hourOfDay: Int,  // 0-23
    val intensity: Float // 0.0 - 1.0
)
