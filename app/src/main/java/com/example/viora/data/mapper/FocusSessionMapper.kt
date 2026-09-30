package com.example.viora.data.mapper

import com.example.viora.data.database.entity.FocusSessionEntity
import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.model.FocusType

fun FocusSessionEntity.toDomain(): FocusSession {
    return FocusSession(
        id = id,
        type = FocusType.valueOf(type),
        startTime = startTime,
        endTime = endTime,
        plannedDurationMinutes = plannedDurationMinutes,
        actualDurationMinutes = actualDurationMinutes,
        completed = completed,
        interruptedCount = interruptedCount,
        ambientSound = ambientSound,
        date = date
    )
}

fun FocusSession.toEntity(): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        type = type.name,
        startTime = startTime,
        endTime = endTime,
        plannedDurationMinutes = plannedDurationMinutes,
        actualDurationMinutes = actualDurationMinutes,
        completed = completed,
        interruptedCount = interruptedCount,
        ambientSound = ambientSound,
        date = date
    )
}
