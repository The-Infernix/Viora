package com.example.viora.domain.model

data class FocusSession(
    val id: Long = 0,
    val type: FocusType,
    val startTime: Long,
    val endTime: Long? = null,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int? = null,
    val completed: Boolean = false,
    val interruptedCount: Int = 0,
    val ambientSound: String? = null,
    val date: String
)

enum class FocusType(val displayName: String) {
    DEEP_WORK("Deep Work"),
    POMODORO("Pomodoro"),
    QUICK_FOCUS("Quick Focus"),
    SCHEDULED("Scheduled")
}
