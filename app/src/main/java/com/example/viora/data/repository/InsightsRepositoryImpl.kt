package com.example.viora.data.repository

import com.example.viora.data.database.dao.DailySummaryDao
import com.example.viora.data.database.dao.UsageEventDao
import com.example.viora.data.mapper.toDomain
import com.example.viora.data.mapper.toEntity
import com.example.viora.domain.model.DailySummary
import com.example.viora.domain.model.ReportGrade
import com.example.viora.domain.model.WeeklyReport
import com.example.viora.domain.repository.InsightsRepository
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsRepositoryImpl @Inject constructor(
    private val dailySummaryDao: DailySummaryDao,
    private val usageEventDao: UsageEventDao,
    private val settingsRepository: SettingsRepository
) : InsightsRepository {

    override fun getDailySummary(date: String): Flow<DailySummary?> {
        return dailySummaryDao.getSummary(date).map { it?.toDomain() }
    }

    override fun getWeeklyReport(weekStartDate: String): Flow<WeeklyReport> {
        val weekEndDate = TimeUtils.todayString()
        val prevWeekStart = TimeUtils.lastNWeeksAgo(1)
        val prevWeekEnd = TimeUtils.daysAgo(7)

        return dailySummaryDao.getSummariesBetween(weekStartDate, weekEndDate).map { summaries ->
            val prevSummaries = dailySummaryDao.getSummariesBetween(prevWeekStart, prevWeekEnd)

            val totalScreenTime = summaries.sumOf { it.totalScreenTimeMinutes }
            val days = summaries.size.coerceAtLeast(1)
            val avgDaily = totalScreenTime / days

            val totalPickups = summaries.sumOf { it.totalPickups }
            val goalMetDays = summaries.count { it.goalMet }
            val streakDays = summaries.count { it.streakDay }

            val grade = when {
                avgDaily <= 60 && goalMetDays >= 6 -> ReportGrade.A_PLUS
                avgDaily <= 90 && goalMetDays >= 5 -> ReportGrade.A
                avgDaily <= 120 && goalMetDays >= 4 -> ReportGrade.B_PLUS
                avgDaily <= 150 && goalMetDays >= 3 -> ReportGrade.B
                avgDaily <= 210 -> ReportGrade.C
                else -> ReportGrade.D
            }

            WeeklyReport(
                weekStartDate = weekStartDate,
                weekEndDate = weekEndDate,
                avgDailyScreenTimeMinutes = avgDaily,
                totalScreenTimeMinutes = totalScreenTime,
                changeFromPreviousWeek = 0,
                totalPickups = totalPickups,
                avgPickupsPerDay = totalPickups / days,
                totalFocusMinutes = summaries.sumOf { it.focusMinutes },
                focusSessionsCompleted = summaries.sumOf { it.focusSessionsCount },
                goalMetDays = goalMetDays,
                currentStreak = streakDays,
                bestStreak = streakDays,
                topApp = null,
                grade = grade
            )
        }
    }

    override fun getSummariesBetween(startDate: String, endDate: String): Flow<List<DailySummary>> {
        return dailySummaryDao.getSummariesBetween(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun calculateAndSaveDailySummary(date: String) {
        val events = usageEventDao.getEventsForDate(date).first()
        val totalSeconds = events.sumOf { it.durationSeconds }
        val goal = settingsRepository.getDailyGoal().first()

        val appUsage = events.groupBy { it.packageName }
            .mapValues { (_, evts) -> evts.sumOf { it.durationSeconds } }
            .maxByOrNull { it.value }

        val peakHour = events.groupBy { it.hourOfDay }
            .maxByOrNull { (_, evts) -> evts.sumOf { it.durationSeconds } }
            ?.key ?: 0

        val summary = DailySummary(
            date = date,
            totalScreenTimeMinutes = totalSeconds / 60,
            totalPickups = 0,
            totalUnlocks = 0,
            wellbeingScore = 50,
            focusMinutes = 0,
            focusSessionsCount = 0,
            mostUsedApp = appUsage?.key,
            mostUsedAppMinutes = (appUsage?.value ?: 0) / 60,
            peakHour = peakHour,
            interventionsTriggered = events.count { it.wasInterrupted },
            dailyGoalMinutes = goal,
            goalMet = (totalSeconds / 60) <= goal,
            streakDay = (totalSeconds / 60) <= goal,
            calculatedAt = System.currentTimeMillis()
        )
        dailySummaryDao.insertSummary(summary.toEntity())
    }
}
