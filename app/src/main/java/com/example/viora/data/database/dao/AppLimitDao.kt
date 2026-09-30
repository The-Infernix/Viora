package com.example.viora.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.viora.data.database.entity.AppLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {

    @Query("SELECT * FROM app_limits ORDER BY appName")
    fun getAllLimits(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE isEnabled = 1")
    fun getActiveLimits(): Flow<List<AppLimitEntity>>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName")
    fun getLimitForApp(packageName: String): Flow<AppLimitEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM app_limits WHERE packageName = :packageName)")
    fun isAppMonitored(packageName: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLimit(limit: AppLimitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLimits(limits: List<AppLimitEntity>)

    @Delete
    suspend fun deleteLimit(limit: AppLimitEntity)

    @Query("DELETE FROM app_limits WHERE packageName = :packageName")
    suspend fun deleteLimitByPackage(packageName: String)
}
