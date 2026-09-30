package com.example.viora.domain.usecase.dashboard

import com.example.viora.domain.model.AppUsage
import com.example.viora.domain.model.SessionTimelineEntry
import com.example.viora.domain.model.WellbeingLevel
import com.example.viora.domain.model.WellbeingScore
import com.example.viora.domain.model.WeeklyReport
import com.example.viora.domain.repository.InsightsRepository
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.domain.repository.UsageRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class DashboardData(
    val screenTimeMinutes: Int,
    val dailyGoalMinutes: Int,
    val goalProgress: Float,
    val wellbeingScore: WellbeingScore,
    val weeklyChangePercent: Int,
    val currentStreak: Int,
    val timeSavedMinutes: Int,
    val topApps: List<AppUsage>,
    val sessionTimeline: List<SessionTimelineEntry>,
    val todaysPickups: Int,
    val weeklyTotals: List<Pair<String, Int>>
)

class GetDashboardUseCase @Inject constructor(
    private val usageRepository: UsageRepository,
    private val insightsRepository: InsightsRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        val today = TimeUtils.todayString()
        val weekStart = TimeUtils.currentWeekStart()

        val baseFlow = combine(
            usageRepository.getTotalScreenTime(today),
            usageRepository.getDailyAppBreakdown(today),
            usageRepository.getSessionTimeline(today),
            settingsRepository.getDailyGoal(),
            insightsRepository.getDailySummary(today)
        ) { screenTime, appBreakdown, timeline, goal, summary ->
            Triple(screenTime, Triple(appBreakdown, timeline, goal), summary)
        }

        val weeklyFlow = usageRepository.getDailyTotals(weekStart, today)

        return baseFlow.combine(weeklyFlow) { base, weeklyTotals ->
            val (screenTime, middle, summary) = base
            val (appBreakdown, timeline, goal) = middle
            val pickups = summary?.totalPickups ?: 0
            val focusMinutes = summary?.focusMinutes ?: 0

            val score = calculateWellbeingScore(screenTime / 60, goal, pickups, focusMinutes)

            DashboardData(
                screenTimeMinutes = screenTime / 60,
                dailyGoalMinutes = goal,
                goalProgress = ((screenTime / 60f) / goal).coerceIn(0f, 1f),
                wellbeingScore = score,
                weeklyChangePercent = 0,
                currentStreak = 0,
                timeSavedMinutes = 0,
                topApps = appBreakdown.take(5),
                sessionTimeline = timeline,
                todaysPickups = pickups,
                weeklyTotals = weeklyTotals
            )
        }
    }

    private fun calculateWellbeingScore(
        screenTimeMinutes: Int,
        goalMinutes: Int,
        pickups: Int,
        focusMinutes: Int
    ): WellbeingScore {
        var score = 100

        if (goalMinutes > 0) {
            val ratio = screenTimeMinutes.toFloat() / goalMinutes
            when {
                ratio > 1.5f -> score -= 40
                ratio > 1.2f -> score -= 25
                ratio > 1.0f -> score -= 15
            }
        }

        when {
            pickups > 80 -> score -= 20
            pickups > 50 -> score -= 10
            pickups > 30 -> score -= 5
        }

        score += (focusMinutes / 10).coerceAtMost(15)
        score = score.coerceIn(0, 100)

        val level = when {
            score >= 85 -> WellbeingLevel.EXCELLENT
            score >= 70 -> WellbeingLevel.GOOD
            score >= 50 -> WellbeingLevel.FAIR
            score >= 30 -> WellbeingLevel.NEEDS_WORK
            else -> WellbeingLevel.POOR
        }

        return WellbeingScore(
            score = score,
            level = level,
            message = getScoreMessage(level),
            changeFromYesterday = 0
        )
    }

    private fun getScoreMessage(level: WellbeingLevel): String = when (level) {
        WellbeingLevel.EXCELLENT -> "You're having a great digital day!"
        WellbeingLevel.GOOD -> "Looking good. Keep it up!"
        WellbeingLevel.FAIR -> "You're doing okay. Room to improve."
        WellbeingLevel.NEEDS_WORK -> "Consider taking a break."
        WellbeingLevel.POOR -> "Time for a digital detox?"
    }
}
