package com.example.viora.domain.model

data class UsageEvent(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val appCategory: AppCategory,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val date: String,
    val hourOfDay: Int,
    val wasInterrupted: Boolean = false,
    val interventionLevel: Int = 0
)

enum class AppCategory(val displayName: String) {
    SOCIAL("Social"),
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity"),
    GAMES("Games"),
    NEWS("News"),
    SHOPPING("Shopping"),
    UTILITIES("Utilities"),
    COMMUNICATION("Communication"),
    OTHER("Other")
}
