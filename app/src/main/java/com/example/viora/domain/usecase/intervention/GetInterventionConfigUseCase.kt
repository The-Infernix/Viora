package com.example.viora.domain.usecase.intervention

import com.example.viora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class InterventionConfig(
    val enabled: Boolean,
    val level1Minutes: Int,
    val level2Minutes: Int,
    val level3Minutes: Int,
    val level4Minutes: Int,
    val intentCheckEnabled: Boolean,
    val progressiveFrictionEnabled: Boolean
)

class GetInterventionConfigUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<InterventionConfig> = kotlinx.coroutines.flow.flow {
        val enabled = settingsRepository.isInterventionsEnabled().first()
        val levels = settingsRepository.getInterventionLevels().first()
        val intentCheck = settingsRepository.isIntentCheckEnabled().first()
        val friction = settingsRepository.isProgressiveFrictionEnabled().first()

        emit(InterventionConfig(
            enabled = enabled,
            level1Minutes = levels.level1,
            level2Minutes = levels.level2,
            level3Minutes = levels.level3,
            level4Minutes = levels.level4,
            intentCheckEnabled = intentCheck,
            progressiveFrictionEnabled = friction
        ))
    }
}
