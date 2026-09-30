package com.example.viora.domain.repository

import com.example.viora.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun getRecentSessions(limit: Int): Flow<List<FocusSession>>
    fun getSessionsForDate(date: String): Flow<List<FocusSession>>
    fun getCompletedSessionsCount(startDate: String, endDate: String): Flow<Int>
    fun getTotalFocusMinutes(startDate: String, endDate: String): Flow<Int>
    fun getActiveSession(): Flow<FocusSession?>
    suspend fun startSession(session: FocusSession): Long
    suspend fun updateSession(session: FocusSession)
    suspend fun endSession(id: Long, actualMinutes: Int, interruptedCount: Int, completed: Boolean = true)
}
