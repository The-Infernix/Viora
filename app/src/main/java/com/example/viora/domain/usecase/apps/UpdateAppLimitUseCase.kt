package com.example.viora.domain.usecase.apps

import com.example.viora.domain.model.AppCategory
import com.example.viora.domain.model.AppLimit
import com.example.viora.domain.model.InterventionLevel
import com.example.viora.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateAppLimitUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        packageName: String,
        appName: String,
        dailyLimitMinutes: Int,
        category: AppCategory,
        interventionLevel: InterventionLevel = InterventionLevel.REFLECT,
        isEnabled: Boolean = true,
        customMessage: String? = null
    ) {
        settingsRepository.upsertAppLimit(
            AppLimit(
                packageName = packageName,
                appName = appName,
                dailyLimitMinutes = dailyLimitMinutes,
                category = category,
                interventionLevel = interventionLevel,
                isEnabled = isEnabled,
                customMessage = customMessage
            )
        )
    }
}
