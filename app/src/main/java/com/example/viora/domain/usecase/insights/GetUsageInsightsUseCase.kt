package com.example.viora.domain.usecase.insights

import com.example.viora.domain.model.AppUsage
import com.example.viora.domain.model.SessionTimelineEntry
import com.example.viora.domain.model.UsageHeatmapData
import com.example.viora.domain.repository.UsageRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class UsageInsights(
    val totalScreenTimeMinutes: Int,
    val avgDailyMinutes: Int,
    val totalPickups: Int,
    val mostUsedApp: AppUsage?,
    val peakHour: Int,
    val appBreakdown: List<AppUsage>,
    val heatmapData: List<UsageHeatmapData>,
    val hourlyUsage: List<Pair<Int, Int>>,
    val sessionTimeline: List<SessionTimelineEntry>
)

class GetUsageInsightsUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    operator fun invoke(period: InsightPeriod = InsightPeriod.WEEK): Flow<UsageInsights> {
        val today = TimeUtils.todayString()
        val startDate = when (period) {
            InsightPeriod.DAY -> today
            InsightPeriod.WEEK -> TimeUtils.currentWeekStart()
            InsightPeriod.MONTH -> TimeUtils.currentMonthStart()
        }

        return combine(
            usageRepository.getDailyAppBreakdown(today),
            usageRepository.getHourlyUsage(startDate, today),
            usageRepository.getDailyTotals(startDate, today),
            usageRepository.getUsageHeatmap(startDate, today),
            usageRepository.getSessionTimeline(today)
        ) { appBreakdown, hourlyUsage, dailyTotals, heatmap, sessionTimeline ->
            val totalMinutes = dailyTotals.sumOf { it.second } / 60
            val days = dailyTotals.size.coerceAtLeast(1)

            UsageInsights(
                totalScreenTimeMinutes = totalMinutes,
                avgDailyMinutes = totalMinutes / days,
                totalPickups = 0,
                mostUsedApp = appBreakdown.firstOrNull(),
                peakHour = hourlyUsage.maxByOrNull { it.second }?.first ?: 0,
                appBreakdown = appBreakdown,
                heatmapData = heatmap,
                hourlyUsage = hourlyUsage,
                sessionTimeline = sessionTimeline
            )
        }
    }
}

enum class InsightPeriod {
    DAY, WEEK, MONTH
}
