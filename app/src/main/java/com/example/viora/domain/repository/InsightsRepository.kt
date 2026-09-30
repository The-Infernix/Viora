package com.example.viora.domain.repository

import com.example.viora.domain.model.DailySummary
import com.example.viora.domain.model.WeeklyReport
import kotlinx.coroutines.flow.Flow

interface InsightsRepository {
    fun getDailySummary(date: String): Flow<DailySummary?>
    fun getWeeklyReport(weekStartDate: String): Flow<WeeklyReport>
    fun getSummariesBetween(startDate: String, endDate: String): Flow<List<DailySummary>>
    suspend fun calculateAndSaveDailySummary(date: String)
}
