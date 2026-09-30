package com.example.viora.domain.model

data class Insight(
    val id: String,
    val title: String,
    val message: String,
    val type: InsightType,
    val actionable: Boolean = false,
    val actionLabel: String? = null
)

enum class InsightType {
    DAILY_TIP,
    PATTERN_DETECTION,
    ACHIEVEMENT,
    SUGGESTION,
    WARNING
}
