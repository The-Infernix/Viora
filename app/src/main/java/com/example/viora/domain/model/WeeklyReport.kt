package com.example.viora.domain.model

data class WeeklyReport(
    val weekStartDate: String,
    val weekEndDate: String,
    val avgDailyScreenTimeMinutes: Int,
    val totalScreenTimeMinutes: Int,
    val changeFromPreviousWeek: Int,
    val totalPickups: Int,
    val avgPickupsPerDay: Int,
    val totalFocusMinutes: Int,
    val focusSessionsCompleted: Int,
    val goalMetDays: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val topApp: AppUsage?,
    val grade: ReportGrade
)

enum class ReportGrade(val label: String, val emoji: String) {
    A_PLUS("A+", "Excellent"),
    A("A", "Great"),
    B_PLUS("B+", "Good"),
    B("B", "Fair"),
    C("C", "Needs improvement"),
    D("D", "Poor")
}
