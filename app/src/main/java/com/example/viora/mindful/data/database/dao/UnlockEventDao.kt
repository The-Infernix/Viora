package com.example.viora.mindful.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.viora.mindful.data.database.entity.UnlockEventEntity
import kotlinx.coroutines.flow.Flow

data class ReasonCountTuple(
    val selectedReasonId: String,
    val selectedReasonLabel: String,
    val count: Int
)

data class HourlyCountTuple(
    val hourOfDay: Int,
    val count: Int
)

data class DailyCountTuple(
    val date: String,
    val count: Int,
    val promptedCount: Int,
    val justCheckingCount: Int
)

@Dao
interface UnlockEventDao {

    @Query("SELECT COUNT(*) FROM unlock_events WHERE date = :date")
    suspend fun getCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM unlock_events WHERE date = :date AND promptShown = 1")
    suspend fun getPromptedCountForDate(date: String): Int

    @Query("SELECT COUNT(*) FROM unlock_events WHERE date = :date AND wasJustChecking = 1")
    suspend fun getJustCheckingCountForDate(date: String): Int

    @Query("""
        SELECT selectedReasonId, selectedReasonLabel, COUNT(*) as count
        FROM unlock_events WHERE date BETWEEN :startDate AND :endDate
        GROUP BY selectedReasonId ORDER BY count DESC
    """)
    fun getReasonBreakdown(startDate: String, endDate: String): Flow<List<ReasonCountTuple>>

    @Query("""
        SELECT hourOfDay, COUNT(*) as count
        FROM unlock_events WHERE date = :date
        GROUP BY hourOfDay ORDER BY count DESC
    """)
    fun getHourlyBreakdown(date: String): Flow<List<HourlyCountTuple>>

    @Query("""
        SELECT date, COUNT(*) as count,
            SUM(CASE WHEN promptShown = 1 THEN 1 ELSE 0 END) as promptedCount,
            SUM(CASE WHEN wasJustChecking = 1 THEN 1 ELSE 0 END) as justCheckingCount
        FROM unlock_events WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date ORDER BY date
    """)
    fun getDailyCounts(startDate: String, endDate: String): Flow<List<DailyCountTuple>>

    @Query("SELECT COUNT(*) FROM unlock_events WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCountBetween(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM unlock_events WHERE wasJustChecking = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun getJustCheckingCountBetween(startDate: String, endDate: String): Int

    @Query("""
        SELECT selectedReasonLabel FROM unlock_events
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY selectedReasonId ORDER BY COUNT(*) DESC LIMIT 1
    """)
    suspend fun getMostCommonReason(startDate: String, endDate: String): String?

    @Query("""
        SELECT hourOfDay FROM unlock_events
        WHERE date = :date
        GROUP BY hourOfDay ORDER BY COUNT(*) DESC LIMIT 1
    """)
    suspend fun getPeakHour(date: String): Int?

    @Query("SELECT COUNT(*) FROM unlock_events WHERE promptShown = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun getPromptsShownBetween(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM unlock_events WHERE lockedPhoneAgain = 1 AND promptShown = 1 AND date BETWEEN :startDate AND :endDate")
    suspend fun getLockedAgainCountBetween(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM unlock_events WHERE promptShown = 1")
    suspend fun getTotalPromptsShown(): Int

    @Query("""
        SELECT COUNT(*) FROM (
            SELECT wasJustChecking FROM unlock_events
            WHERE promptShown = 1
            ORDER BY timestamp DESC LIMIT 5
        ) WHERE wasJustChecking = 1
    """)
    suspend fun getRecentConsecutiveJustChecking(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UnlockEventEntity)

    @Query("DELETE FROM unlock_events WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
