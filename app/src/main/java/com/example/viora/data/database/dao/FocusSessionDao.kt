package com.example.viora.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.viora.data.database.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 20): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsForDate(date: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE completed = 1 AND date BETWEEN :startDate AND :endDate")
    fun getCompletedSessionsCount(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT COALESCE(SUM(actualDurationMinutes), 0) FROM focus_sessions WHERE completed = 1 AND date BETWEEN :startDate AND :endDate")
    fun getTotalFocusMinutes(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT * FROM focus_sessions WHERE completed = 0 AND endTime IS NULL LIMIT 1")
    fun getActiveSession(): Flow<FocusSessionEntity?>

    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateSession(session: FocusSessionEntity)
}
