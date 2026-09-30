package com.example.viora.domain.usecase.apps

import com.example.viora.domain.model.AppLimit
import com.example.viora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonitoredAppsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<List<AppLimit>> {
        return settingsRepository.getActiveAppLimits()
    }
}
