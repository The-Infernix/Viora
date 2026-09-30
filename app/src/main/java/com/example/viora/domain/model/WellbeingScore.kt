package com.example.viora.domain.model

data class WellbeingScore(
    val score: Int,
    val level: WellbeingLevel,
    val message: String,
    val changeFromYesterday: Int
)

enum class WellbeingLevel(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    NEEDS_WORK("Needs Work"),
    POOR("Poor")
}
