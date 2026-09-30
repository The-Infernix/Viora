package com.example.viora.data.repository

import com.example.viora.data.database.dao.DailySummaryDao
import com.example.viora.data.database.dao.UsageEventDao
import com.example.viora.data.mapper.toDomain
import com.example.viora.data.mapper.toEntity
import com.example.viora.domain.model.AppUsage
import com.example.viora.domain.model.DailySummary
import com.example.viora.domain.model.AppCategory
import com.example.viora.domain.model.SessionTimelineEntry
import com.example.viora.domain.model.UsageEvent
import com.example.viora.domain.model.UsageHeatmapData
import com.example.viora.domain.repository.UsageRepository
import com.example.viora.util.AppCategoryDetector
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepositoryImpl @Inject constructor(
    private val usageEventDao: UsageEventDao,
    private val dailySummaryDao: DailySummaryDao
) : UsageRepository {

    override fun getEventsForDate(date: String): Flow<List<UsageEvent>> {
        return usageEventDao.getEventsForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDailyAppBreakdown(date: String): Flow<List<AppUsage>> {
        return usageEventDao.getDailyAppBreakdown(date).map { tuples ->
            tuples.map { tuple ->
                AppUsage(
                    packageName = tuple.packageName,
                    appName = tuple.appName,
                    durationMinutes = tuple.totalSeconds / 60,
                    category = AppCategoryDetector.detect(tuple.packageName)
                )
            }
        }
    }

    override fun getTotalScreenTime(date: String): Flow<Int> {
        return usageEventDao.getTotalScreenTime(date).map { it ?: 0 }
    }

    override fun getHourlyUsage(startDate: String, endDate: String): Flow<List<Pair<Int, Int>>> {
        return usageEventDao.getHourlyUsage(startDate, endDate).map { tuples ->
            tuples.map { it.hourOfDay to it.totalSeconds }
        }
    }

    override fun getDailyTotals(startDate: String, endDate: String): Flow<List<Pair<String, Int>>> {
        return usageEventDao.getDailyTotals(startDate, endDate).map { tuples ->
            tuples.map { it.date to it.totalSeconds }
        }
    }

    override fun getSessionTimeline(date: String): Flow<List<SessionTimelineEntry>> {
        return usageEventDao.getEventsForDate(date).map { entities ->
            entities.map { entity ->
                SessionTimelineEntry(
                    packageName = entity.packageName,
                    appName = entity.appName,
                    startHour = entity.hourOfDay,
                    durationMinutes = entity.durationSeconds / 60,
                    category = AppCategory.valueOf(entity.appCategory)
                )
            }
        }
    }

    override fun getUsageHeatmap(startDate: String, endDate: String): Flow<List<UsageHeatmapData>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return usageEventDao.getDailyHourlyUsage(startDate, endDate).map { tuples ->
            val maxUsage = tuples.maxOfOrNull { it.totalSeconds } ?: 1
            tuples.map { tuple ->
                val cal = dateFormat.parse(tuple.date)?.let {
                    Calendar.getInstance().apply { time = it }
                } ?: Calendar.getInstance()
                // Calendar.SUNDAY=1 .. SATURDAY=7 → map to 0=Monday..6=Sunday
                val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
                UsageHeatmapData(
                    dayOfWeek = dayOfWeek,
                    hourOfDay = tuple.hourOfDay,
                    intensity = tuple.totalSeconds.toFloat() / maxUsage
                )
            }
        }
    }

    override fun getRecentSummaries(days: Int): Flow<List<DailySummary>> {
        val startDate = TimeUtils.lastNDays(days).first()
        val endDate = TimeUtils.todayString()
        return dailySummaryDao.getSummariesBetween(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun recordEvent(event: UsageEvent) {
        usageEventDao.insertEvent(event.toEntity())
    }

    override suspend fun recordEvents(events: List<UsageEvent>) {
        usageEventDao.insertEvents(events.map { it.toEntity() })
    }

    override suspend fun cleanupOldData(olderThanDays: Int) {
        val cutoff = com.example.viora.util.TimeUtils.daysAgo(olderThanDays)
        usageEventDao.deleteOlderThan(cutoff)
    }

}
