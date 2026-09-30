package com.example.viora.domain.usecase.dashboard

import com.example.viora.domain.repository.InsightsRepository
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class CalculateStreakUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Int> {
        val recentDays = TimeUtils.lastNDays(30)
        return combine(
            insightsRepository.getSummariesBetween(recentDays.first(), recentDays.last()),
            settingsRepository.getDailyGoal()
        ) { summaries, goal ->
            var streak = 0
            for (summary in summaries.reversed()) {
                if (summary.totalScreenTimeMinutes <= goal) {
                    streak++
                } else {
                    break
                }
            }
            streak
        }
    }
}
