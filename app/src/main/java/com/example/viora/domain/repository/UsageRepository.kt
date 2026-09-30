package com.example.viora.domain.repository

import com.example.viora.domain.model.AppUsage
import com.example.viora.domain.model.DailySummary
import com.example.viora.domain.model.SessionTimelineEntry
import com.example.viora.domain.model.UsageEvent
import com.example.viora.domain.model.UsageHeatmapData
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getEventsForDate(date: String): Flow<List<UsageEvent>>
    fun getDailyAppBreakdown(date: String): Flow<List<AppUsage>>
    fun getTotalScreenTime(date: String): Flow<Int>
    fun getHourlyUsage(startDate: String, endDate: String): Flow<List<Pair<Int, Int>>>
    fun getDailyTotals(startDate: String, endDate: String): Flow<List<Pair<String, Int>>>
    fun getSessionTimeline(date: String): Flow<List<SessionTimelineEntry>>
    fun getUsageHeatmap(startDate: String, endDate: String): Flow<List<UsageHeatmapData>>
    fun getRecentSummaries(days: Int): Flow<List<DailySummary>>
    suspend fun recordEvent(event: UsageEvent)
    suspend fun recordEvents(events: List<UsageEvent>)
    suspend fun cleanupOldData(olderThanDays: Int = 90)
}
