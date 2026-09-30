package com.example.viora.domain.usecase.insights

import com.example.viora.domain.model.WeeklyReport
import com.example.viora.domain.repository.InsightsRepository
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.domain.repository.UsageRepository
import com.example.viora.domain.repository.FocusRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetWeeklyReportUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository,
    private val usageRepository: UsageRepository,
    private val focusRepository: FocusRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<WeeklyReport> {
        val weekStart = TimeUtils.currentWeekStart()
        val weekEnd = TimeUtils.todayString()
        val prevWeekEnd = TimeUtils.daysAgo(7)
        val prevWeekStart = TimeUtils.daysAgo(13)

        val summariesFlow = insightsRepository.getSummariesBetween(weekStart, weekEnd)
        val dailyTotalsFlow = usageRepository.getDailyTotals(weekStart, weekEnd)
        val prevDailyTotalsFlow = usageRepository.getDailyTotals(prevWeekStart, prevWeekEnd)
        val focusCountFlow = focusRepository.getCompletedSessionsCount(weekStart, weekEnd)
        val focusMinutesFlow = focusRepository.getTotalFocusMinutes(weekStart, weekEnd)
        val goalFlow = settingsRepository.getDailyGoal()

        return combine(summariesFlow, dailyTotalsFlow, prevDailyTotalsFlow, focusCountFlow, focusMinutesFlow) { summaries, dailyTotals, prevDailyTotals, focusCount, focusMinutes ->
            Triple(summaries, dailyTotals, Triple(prevDailyTotals, focusCount, focusMinutes))
        }.combine(goalFlow) { (summaries, dailyTotals, triple), goal ->
            val (prevDailyTotals, focusCount, focusMinutes) = triple

            val totalScreenTime = dailyTotals.sumOf { it.second } / 60
            val days = dailyTotals.size.coerceAtLeast(1)
            val avgDaily = totalScreenTime / days

            val prevTotalScreenTime = prevDailyTotals.sumOf { it.second } / 60
            val changeFromPrev = if (prevTotalScreenTime > 0) {
                ((totalScreenTime - prevTotalScreenTime).toFloat() / prevTotalScreenTime * 100).toInt()
            } else 0

            val goalMetDays = summaries.count { it.goalMet }
            val streak = calculateCurrentStreak(summaries, goal)

            WeeklyReport(
                weekStartDate = weekStart,
                weekEndDate = weekEnd,
                avgDailyScreenTimeMinutes = avgDaily,
                totalScreenTimeMinutes = totalScreenTime,
                changeFromPreviousWeek = changeFromPrev,
                totalPickups = summaries.sumOf { it.totalPickups },
                avgPickupsPerDay = summaries.sumOf { it.totalPickups } / days,
                totalFocusMinutes = focusMinutes,
                focusSessionsCompleted = focusCount,
                goalMetDays = goalMetDays,
                currentStreak = streak,
                bestStreak = streak,
                topApp = null,
                grade = calculateGrade(avgDaily, goal, goalMetDays, focusCount)
            )
        }
    }

    private fun calculateCurrentStreak(summaries: List<com.example.viora.domain.model.DailySummary>, goal: Int): Int {
        var streak = 0
        for (summary in summaries.reversed()) {
            if (summary.totalScreenTimeMinutes <= goal) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateGrade(avgMinutes: Int, goal: Int, goalMetDays: Int, focusSessions: Int): com.example.viora.domain.model.ReportGrade {
        var score = 0
        if (goal > 0) {
            val ratio = avgMinutes.toFloat() / goal
            when {
                ratio <= 0.7f -> score += 40
                ratio <= 0.9f -> score += 30
                ratio <= 1.0f -> score += 20
                else -> score += 10
            }
        }
        score += (goalMetDays * 5).coerceAtMost(30)
        score += (focusSessions * 5).coerceAtMost(30)

        return when {
            score >= 90 -> com.example.viora.domain.model.ReportGrade.A_PLUS
            score >= 80 -> com.example.viora.domain.model.ReportGrade.A
            score >= 70 -> com.example.viora.domain.model.ReportGrade.B_PLUS
            score >= 60 -> com.example.viora.domain.model.ReportGrade.B
            score >= 45 -> com.example.viora.domain.model.ReportGrade.C
            else -> com.example.viora.domain.model.ReportGrade.D
        }
    }
}
