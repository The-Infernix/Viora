package com.example.viora.mindful.data.repository

import android.app.NotificationManager
import android.content.Context
import com.example.viora.mindful.data.database.dao.UnlockEventDao
import com.example.viora.mindful.data.datastore.MindfulUnlockPreferences
import com.example.viora.mindful.domain.model.DailyUnlockCount
import com.example.viora.mindful.domain.model.UnlockEvent
import com.example.viora.mindful.domain.model.UnlockFrequency
import com.example.viora.mindful.domain.model.UnlockReason
import com.example.viora.mindful.domain.model.UnlockStats
import com.example.viora.mindful.domain.model.WeeklyUnlockReport
import com.example.viora.mindful.domain.repository.MindfulUnlockRepository
import com.example.viora.mindful.data.database.entity.UnlockEventEntity
import com.example.viora.util.TimeUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MindfulUnlockRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: MindfulUnlockPreferences,
    private val unlockEventDao: UnlockEventDao
) : MindfulUnlockRepository {

    override fun isEnabled(): Flow<Boolean> = preferences.isEnabled

    override fun getFrequency(): Flow<UnlockFrequency> = preferences.frequency.map { name ->
        try { UnlockFrequency.valueOf(name) } catch (_: Exception) { UnlockFrequency.OCCASIONAL }
    }

    override fun getFrequencyThreshold(): Flow<Int> = preferences.frequencyThreshold

    override fun getCustomReasons(): Flow<List<UnlockReason>> = preferences.customReasons.map { jsonSet ->
        if (jsonSet.isEmpty()) {
            UnlockReason.DEFAULT_REASONS
        } else {
            jsonSet.map { encoded ->
                val parts = encoded.split("|")
                UnlockReason(
                    id = parts.getOrElse(0) { "" },
                    label = parts.getOrElse(1) { "" },
                    icon = parts.getOrElse(2) { "other" },
                    isBuiltIn = false
                )
            }
        }
    }

    override fun getHapticsEnabled(): Flow<Boolean> = preferences.hapticsEnabled

    override fun getAnimationSpeed(): Flow<Float> = preferences.animationSpeed

    override fun getShowStats(): Flow<Boolean> = preferences.showStats

    override fun isReasonCustomized(): Flow<Boolean> = preferences.customReasons.map { it.isNotEmpty() }

    override suspend fun setEnabled(enabled: Boolean) = preferences.setEnabled(enabled)

    override suspend fun setFrequency(frequency: UnlockFrequency) = preferences.setFrequency(frequency.name)

    override suspend fun setFrequencyThreshold(threshold: Int) = preferences.setFrequencyThreshold(threshold)

    override suspend fun setCustomReasons(reasons: List<UnlockReason>) {
        val encoded = reasons.map { "${it.id}|${it.label}|${it.icon}" }.toSet()
        preferences.setCustomReasons(encoded)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) = preferences.setHapticsEnabled(enabled)

    override suspend fun setAnimationSpeed(speed: Float) = preferences.setAnimationSpeed(speed)

    override suspend fun setShowStats(show: Boolean) = preferences.setShowStats(show)

    override suspend fun getTodayUnlockCount(): Int {
        return unlockEventDao.getCountForDate(TimeUtils.todayString())
    }

    override suspend fun getTodayPromptedCount(): Int {
        return unlockEventDao.getPromptedCountForDate(TimeUtils.todayString())
    }

    override suspend fun getConsecutiveJustChecking(): Int {
        return unlockEventDao.getRecentConsecutiveJustChecking()
    }

    override suspend fun recordUnlockEvent(event: UnlockEvent) {
        val entity = UnlockEventEntity(
            timestamp = event.timestamp,
            date = event.date,
            hourOfDay = event.hourOfDay,
            selectedReasonId = event.selectedReasonId,
            selectedReasonLabel = event.selectedReasonLabel,
            wasJustChecking = event.wasJustChecking,
            lockedPhoneAgain = event.lockedPhoneAgain,
            sessionLengthSeconds = event.sessionLengthSeconds,
            openedSocialMediaAfter = event.openedSocialMediaAfter,
            promptShown = event.promptShown,
            promptSkipped = event.promptSkipped
        )
        unlockEventDao.insertEvent(entity)
    }

    override suspend fun incrementConsecutiveJustChecking() {
        val current = preferences.consecutiveJustChecking.first()
        preferences.setConsecutiveJustChecking(current + 1)
    }

    override suspend fun resetConsecutiveJustChecking() {
        preferences.setConsecutiveJustChecking(0)
    }

    override suspend fun shouldShowPrompt(): Boolean {
        val frequency = getFrequency().first()
        val threshold = getFrequencyThreshold().first()
        val todayCount = getTodayUnlockCount()
        val consecutiveJC = getConsecutiveJustChecking()

        return when (frequency) {
            UnlockFrequency.ALWAYS -> true
            UnlockFrequency.NEVER -> false
            UnlockFrequency.AFTER_X_UNLOCKS -> todayCount >= threshold
            UnlockFrequency.LATE_NIGHT_ONLY -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                hour in 22..23 || hour in 0..5
            }
            UnlockFrequency.FOCUS_MODE_ONLY -> {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
            }
            UnlockFrequency.OCCASIONAL -> {
                if (consecutiveJC >= 3) return true
                if (todayCount <= 10) return false
                if (todayCount <= 20) return todayCount % 4 == 0
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (hour in 22..23 || hour in 0..5) return true
                todayCount % 2 == 0
            }
        }
    }

    override suspend fun getUnlockStats(): UnlockStats {
        val today = TimeUtils.todayString()
        val weekAgo = TimeUtils.daysAgo(6)
        val calendar = Calendar.getInstance()

        val totalToday = unlockEventDao.getCountForDate(today)
        val promptedToday = unlockEventDao.getPromptedCountForDate(today)
        val justCheckingToday = unlockEventDao.getJustCheckingCountForDate(today)

        val mostCommonReason = unlockEventDao.getMostCommonReason(weekAgo, today)
        val peakHour = unlockEventDao.getPeakHour(today)

        val weeklyTotal = unlockEventDao.getTotalCountBetween(weekAgo, today)
        val weeklyJC = unlockEventDao.getJustCheckingCountBetween(weekAgo, today)
        val weeklyPrompts = unlockEventDao.getPromptsShownBetween(weekAgo, today)
        val weeklyLockedAgain = unlockEventDao.getLockedAgainCountBetween(weekAgo, today)

        val jcPercentage = if (weeklyTotal > 0) weeklyJC.toFloat() / weeklyTotal else 0f
        val lockAgainRate = if (weeklyPrompts > 0) weeklyLockedAgain.toFloat() / weeklyPrompts else 0f

        val dailyCounts = unlockEventDao.getDailyCounts(weekAgo, today).first().map {
            DailyUnlockCount(
                date = it.date,
                totalCount = it.count,
                promptedCount = it.promptedCount,
                justCheckingCount = it.justCheckingCount
            )
        }

        val weeklyReport = if (weeklyTotal > 0) {
            WeeklyUnlockReport(
                averageUnlocksPerDay = weeklyTotal.toFloat() / 7f,
                mostCommonReason = mostCommonReason ?: "N/A",
                justCheckingTrend = jcPercentage,
                peakHour = peakHour ?: 12,
                totalPromptsShown = weeklyPrompts,
                lockPhoneAgainRate = lockAgainRate
            )
        } else null

        return UnlockStats(
            totalUnlocksToday = totalToday,
            promptShownToday = promptedToday,
            mostCommonReason = mostCommonReason,
            justCheckingPercentage = jcPercentage,
            intentionalUnlocks = totalToday - justCheckingToday,
            mindlessUnlocks = justCheckingToday,
            mostDistractedHour = peakHour,
            dailyTrend = dailyCounts,
            weeklyReport = weeklyReport
        )
    }

    override suspend fun cleanupOldData(olderThanDays: Int) {
        val cutoffDate = TimeUtils.daysAgo(olderThanDays)
        unlockEventDao.deleteOlderThan(cutoffDate)
    }
}
