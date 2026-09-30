package com.example.viora.data.mapper

import com.example.viora.data.database.entity.AppLimitEntity
import com.example.viora.domain.model.AppCategory
import com.example.viora.domain.model.AppLimit
import com.example.viora.domain.model.InterventionLevel

fun AppLimitEntity.toDomain(): AppLimit {
    return AppLimit(
        packageName = packageName,
        appName = appName,
        dailyLimitMinutes = dailyLimitMinutes,
        interventionLevel = InterventionLevel.entries.getOrElse(interventionLevel) { InterventionLevel.REFLECT },
        isEnabled = isEnabled,
        customMessage = customMessage,
        category = AppCategory.valueOf(category)
    )
}

fun AppLimit.toEntity(): AppLimitEntity {
    return AppLimitEntity(
        packageName = packageName,
        appName = appName,
        dailyLimitMinutes = dailyLimitMinutes,
        interventionLevel = interventionLevel.ordinal,
        isEnabled = isEnabled,
        customMessage = customMessage,
        category = category.name
    )
}
