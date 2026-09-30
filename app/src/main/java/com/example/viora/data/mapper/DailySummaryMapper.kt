package com.example.viora.data.mapper

import com.example.viora.data.database.entity.DailySummaryEntity
import com.example.viora.domain.model.DailySummary

fun DailySummaryEntity.toDomain(): DailySummary {
    return DailySummary(
        date = date,
        totalScreenTimeMinutes = totalScreenTimeMinutes,
        totalPickups = totalPickups,
        totalUnlocks = totalUnlocks,
        wellbeingScore = wellbeingScore,
        focusMinutes = focusMinutes,
        focusSessionsCount = focusSessionsCount,
        mostUsedApp = mostUsedApp,
        mostUsedAppMinutes = mostUsedAppMinutes,
        peakHour = peakHour,
        interventionsTriggered = interventionsTriggered,
        dailyGoalMinutes = dailyGoalMinutes,
        goalMet = goalMet,
        streakDay = streakDay,
        calculatedAt = calculatedAt
    )
}

fun DailySummary.toEntity(): DailySummaryEntity {
    return DailySummaryEntity(
        date = date,
        totalScreenTimeMinutes = totalScreenTimeMinutes,
        totalPickups = totalPickups,
        totalUnlocks = totalUnlocks,
        wellbeingScore = wellbeingScore,
        focusMinutes = focusMinutes,
        focusSessionsCount = focusSessionsCount,
        mostUsedApp = mostUsedApp,
        mostUsedAppMinutes = mostUsedAppMinutes,
        peakHour = peakHour,
        interventionsTriggered = interventionsTriggered,
        dailyGoalMinutes = dailyGoalMinutes,
        goalMet = goalMet,
        streakDay = streakDay,
        calculatedAt = calculatedAt
    )
}
