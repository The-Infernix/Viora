package com.example.viora.data.repository

import com.example.viora.data.database.dao.AppLimitDao
import com.example.viora.data.datastore.UserPreferencesDataStore
import com.example.viora.data.mapper.toDomain
import com.example.viora.data.mapper.toEntity
import com.example.viora.domain.model.InterventionLevels
import com.example.viora.domain.model.AppLimit
import com.example.viora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val preferences: UserPreferencesDataStore,
    private val appLimitDao: AppLimitDao
) : SettingsRepository {

    override fun getDailyGoal(): Flow<Int> = preferences.dailyGoalMinutes

    override fun getMonitoredPackages(): Flow<Set<String>> = preferences.monitoredPackages

    override fun getActiveAppLimits(): Flow<List<AppLimit>> {
        return appLimitDao.getActiveLimits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAppLimit(packageName: String): Flow<AppLimit?> {
        return appLimitDao.getLimitForApp(packageName).map { it?.toDomain() }
    }

    override fun isAppMonitored(packageName: String): Flow<Boolean> {
        return appLimitDao.isAppMonitored(packageName)
    }

    override fun getInterventionLevels(): Flow<InterventionLevels> {
        return combine(
            preferences.level1Minutes,
            preferences.level2Minutes,
            preferences.level3Minutes,
            preferences.level4Minutes
        ) { l1, l2, l3, l4 -> InterventionLevels(l1, l2, l3, l4) }
    }

    override fun isInterventionsEnabled(): Flow<Boolean> = preferences.interventionsEnabled

    override fun isIntentCheckEnabled(): Flow<Boolean> = preferences.intentCheckEnabled

    override fun isProgressiveFrictionEnabled(): Flow<Boolean> = preferences.progressiveFrictionEnabled

    override fun getDefaultFocusDuration(): Flow<Int> = preferences.defaultFocusDuration

    override fun isOnboardingComplete(): Flow<Boolean> = preferences.isOnboardingComplete

    override fun getThemeMode(): Flow<String> = preferences.themeMode

    override fun isNotificationsEnabled(): Flow<Boolean> = preferences.notificationsEnabled

    override fun getQuietHours(): Flow<Pair<Int, Int>> {
        return combine(preferences.quietHoursStart, preferences.quietHoursEnd) { s, e -> s to e }
    }

    override fun getStartOnBoot(): Flow<Boolean> = preferences.startOnBoot

    override suspend fun setDailyGoal(minutes: Int) = preferences.setDailyGoal(minutes)

    override suspend fun addMonitoredPackage(packageName: String) = preferences.addMonitoredPackage(packageName)

    override suspend fun removeMonitoredPackage(packageName: String) = preferences.removeMonitoredPackage(packageName)

    override suspend fun setMonitoredPackages(packages: Set<String>) = preferences.setMonitoredPackages(packages)

    override suspend fun upsertAppLimit(limit: AppLimit) = appLimitDao.upsertLimit(limit.toEntity())

    override suspend fun deleteAppLimit(packageName: String) = appLimitDao.deleteLimitByPackage(packageName)

    override suspend fun setInterventionLevels(l1: Int, l2: Int, l3: Int, l4: Int) = preferences.setInterventionLevels(l1, l2, l3, l4)

    override suspend fun setInterventionsEnabled(enabled: Boolean) = preferences.setInterventionsEnabled(enabled)

    override suspend fun setIntentCheckEnabled(enabled: Boolean) = preferences.setIntentCheckEnabled(enabled)

    override suspend fun setProgressiveFrictionEnabled(enabled: Boolean) = preferences.setProgressiveFrictionEnabled(enabled)

    override suspend fun setDefaultFocusDuration(minutes: Int) = preferences.setDefaultFocusDuration(minutes)

    override suspend fun setOnboardingComplete(complete: Boolean) = preferences.setOnboardingComplete(complete)

    override suspend fun setThemeMode(mode: String) = preferences.setThemeMode(mode)

    override suspend fun setNotificationsEnabled(enabled: Boolean) = preferences.setNotificationsEnabled(enabled)

    override suspend fun setQuietHours(start: Int, end: Int) = preferences.setQuietHours(start, end)

    override suspend fun setStartOnBoot(enabled: Boolean) = preferences.setStartOnBoot(enabled)
}
