package com.example.viora.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.viora.data.database.entity.UsageEventEntity
import kotlinx.coroutines.flow.Flow

data class AppUsageTuple(
    val totalSeconds: Int,
    val packageName: String,
    val appName: String
)

data class HourlyUsageTuple(
    val hourOfDay: Int,
    val totalSeconds: Int
)

data class DailyTotalTuple(
    val date: String,
    val totalSeconds: Int
)

data class DailyHourlyTuple(
    val date: String,
    val hourOfDay: Int,
    val totalSeconds: Int
)

@Dao
interface UsageEventDao {

    @Query("SELECT * FROM usage_events WHERE date = :date ORDER BY startTime DESC")
    fun getEventsForDate(date: String): Flow<List<UsageEventEntity>>

    @Query("SELECT * FROM usage_events WHERE date = :date AND packageName = :packageName ORDER BY startTime DESC")
    fun getEventsForAppOnDate(packageName: String, date: String): Flow<List<UsageEventEntity>>

    @Query("""
        SELECT SUM(durationSeconds) as totalSeconds, packageName, appName 
        FROM usage_events WHERE date = :date 
        GROUP BY packageName ORDER BY totalSeconds DESC
    """)
    fun getDailyAppBreakdown(date: String): Flow<List<AppUsageTuple>>

    @Query("SELECT SUM(durationSeconds) as totalSeconds FROM usage_events WHERE date = :date")
    fun getTotalScreenTime(date: String): Flow<Int?>

    @Query("""
        SELECT hourOfDay, SUM(durationSeconds) as totalSeconds 
        FROM usage_events WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY hourOfDay ORDER BY hourOfDay
    """)
    fun getHourlyUsage(startDate: String, endDate: String): Flow<List<HourlyUsageTuple>>

    @Query("""
        SELECT date, SUM(durationSeconds) as totalSeconds 
        FROM usage_events WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY date ORDER BY date
    """)
    fun getDailyTotals(startDate: String, endDate: String): Flow<List<DailyTotalTuple>>

    @Query("""
        SELECT date, hourOfDay, SUM(durationSeconds) as totalSeconds 
        FROM usage_events WHERE date BETWEEN :startDate AND :endDate 
        GROUP BY date, hourOfDay ORDER BY date, hourOfDay
    """)
    fun getDailyHourlyUsage(startDate: String, endDate: String): Flow<List<DailyHourlyTuple>>

    @Query("SELECT COUNT(*) FROM usage_events WHERE date = :date")
    fun getEventCount(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UsageEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<UsageEventEntity>)

    @Query("DELETE FROM usage_events WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)
}
