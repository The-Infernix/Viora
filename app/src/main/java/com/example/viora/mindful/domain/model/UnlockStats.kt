package com.example.viora.mindful.domain.model

data class UnlockStats(
    val totalUnlocksToday: Int,
    val promptShownToday: Int,
    val mostCommonReason: String?,
    val justCheckingPercentage: Float,
    val intentionalUnlocks: Int,
    val mindlessUnlocks: Int,
    val mostDistractedHour: Int?,
    val dailyTrend: List<DailyUnlockCount>,
    val weeklyReport: WeeklyUnlockReport?
)

data class DailyUnlockCount(
    val date: String,
    val totalCount: Int,
    val promptedCount: Int,
    val justCheckingCount: Int
)

data class WeeklyUnlockReport(
    val averageUnlocksPerDay: Float,
    val mostCommonReason: String,
    val justCheckingTrend: Float,
    val peakHour: Int,
    val totalPromptsShown: Int,
    val lockPhoneAgainRate: Float
)
