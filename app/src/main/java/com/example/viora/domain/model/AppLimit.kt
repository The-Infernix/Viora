package com.example.viora.domain.model

data class AppLimit(
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val interventionLevel: InterventionLevel = InterventionLevel.REFLECT,
    val isEnabled: Boolean = true,
    val customMessage: String? = null,
    val category: AppCategory = AppCategory.OTHER
)
