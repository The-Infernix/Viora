package com.example.viora.data.repository

import com.example.viora.data.database.dao.FocusSessionDao
import com.example.viora.data.mapper.toDomain
import com.example.viora.data.mapper.toEntity
import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) : FocusRepository {

    override fun getRecentSessions(limit: Int): Flow<List<FocusSession>> {
        return focusSessionDao.getRecentSessions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getSessionsForDate(date: String): Flow<List<FocusSession>> {
        return focusSessionDao.getSessionsForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedSessionsCount(startDate: String, endDate: String): Flow<Int> {
        return focusSessionDao.getCompletedSessionsCount(startDate, endDate)
    }

    override fun getTotalFocusMinutes(startDate: String, endDate: String): Flow<Int> {
        return focusSessionDao.getTotalFocusMinutes(startDate, endDate)
    }

    override fun getActiveSession(): Flow<FocusSession?> {
        return focusSessionDao.getActiveSession().map { it?.toDomain() }
    }

    override suspend fun startSession(session: FocusSession): Long {
        return focusSessionDao.insertSession(session.toEntity())
    }

    override suspend fun updateSession(session: FocusSession) {
        focusSessionDao.updateSession(session.toEntity())
    }

    override suspend fun endSession(id: Long, actualMinutes: Int, interruptedCount: Int, completed: Boolean) {
        val existing = focusSessionDao.getActiveSession().first()
        if (existing != null && existing.id == id) {
            val updated = existing.copy(
                endTime = System.currentTimeMillis(),
                actualDurationMinutes = actualMinutes,
                completed = completed,
                interruptedCount = interruptedCount
            )
            focusSessionDao.updateSession(updated)
        } else {
            val now = System.currentTimeMillis()
            val entity = com.example.viora.data.database.entity.FocusSessionEntity(
                id = id,
                type = "DEEP_WORK",
                startTime = now,
                endTime = now,
                plannedDurationMinutes = actualMinutes,
                actualDurationMinutes = actualMinutes,
                completed = completed,
                interruptedCount = interruptedCount,
                date = com.example.viora.util.TimeUtils.todayString()
            )
            focusSessionDao.updateSession(entity)
        }
    }
}
