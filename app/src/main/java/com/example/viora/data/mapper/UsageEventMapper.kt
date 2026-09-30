package com.example.viora.data.mapper

import com.example.viora.data.database.entity.UsageEventEntity
import com.example.viora.domain.model.AppCategory
import com.example.viora.domain.model.UsageEvent

fun UsageEventEntity.toDomain(): UsageEvent {
    return UsageEvent(
        id = id,
        packageName = packageName,
        appName = appName,
        appCategory = AppCategory.valueOf(appCategory),
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        date = date,
        hourOfDay = hourOfDay,
        wasInterrupted = wasInterrupted,
        interventionLevel = interventionLevel
    )
}

fun UsageEvent.toEntity(): UsageEventEntity {
    return UsageEventEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        appCategory = appCategory.name,
        startTime = startTime,
        endTime = endTime,
        durationSeconds = durationSeconds,
        date = date,
        hourOfDay = hourOfDay,
        wasInterrupted = wasInterrupted,
        interventionLevel = interventionLevel
    )
}
