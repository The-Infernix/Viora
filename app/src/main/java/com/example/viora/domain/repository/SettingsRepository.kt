package com.example.viora.domain.repository

import com.example.viora.domain.model.AppLimit
import com.example.viora.domain.model.InterventionLevels
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getDailyGoal(): Flow<Int>
    fun getMonitoredPackages(): Flow<Set<String>>
    fun getActiveAppLimits(): Flow<List<AppLimit>>
    fun getAppLimit(packageName: String): Flow<AppLimit?>
    fun isAppMonitored(packageName: String): Flow<Boolean>
    fun getInterventionLevels(): Flow<InterventionLevels>
    fun isInterventionsEnabled(): Flow<Boolean>
    fun isIntentCheckEnabled(): Flow<Boolean>
    fun isProgressiveFrictionEnabled(): Flow<Boolean>
    fun getDefaultFocusDuration(): Flow<Int>
    fun isOnboardingComplete(): Flow<Boolean>
    fun getThemeMode(): Flow<String>
    fun isNotificationsEnabled(): Flow<Boolean>
    fun getQuietHours(): Flow<Pair<Int, Int>>
    fun getStartOnBoot(): Flow<Boolean>
    suspend fun setDailyGoal(minutes: Int)
    suspend fun addMonitoredPackage(packageName: String)
    suspend fun removeMonitoredPackage(packageName: String)
    suspend fun setMonitoredPackages(packages: Set<String>)
    suspend fun upsertAppLimit(limit: AppLimit)
    suspend fun deleteAppLimit(packageName: String)
    suspend fun setInterventionLevels(l1: Int, l2: Int, l3: Int, l4: Int)
    suspend fun setInterventionsEnabled(enabled: Boolean)
    suspend fun setIntentCheckEnabled(enabled: Boolean)
    suspend fun setProgressiveFrictionEnabled(enabled: Boolean)
    suspend fun setDefaultFocusDuration(minutes: Int)
    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun setThemeMode(mode: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setQuietHours(start: Int, end: Int)
    suspend fun setStartOnBoot(enabled: Boolean)
}
