package com.example.viora.domain.usecase.intervention

import com.example.viora.domain.model.InterventionLevel
import com.example.viora.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class InterventionDecision(
    val shouldIntervene: Boolean,
    val level: InterventionLevel,
    val message: String,
    val appName: String,
    val sessionMinutes: Int
)

class CheckInterventionUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(appName: String, sessionSeconds: Int): Flow<InterventionDecision> = flow {
        val enabled = settingsRepository.isInterventionsEnabled().first()
        if (!enabled) {
            emit(InterventionDecision(false, InterventionLevel.NONE, "", appName, 0))
            return@flow
        }

        val levels = settingsRepository.getInterventionLevels().first()
        val sessionDisplayMinutes = sessionSeconds / 60

        val level = when {
            sessionSeconds >= levels.level4 -> InterventionLevel.PROTECT
            sessionSeconds >= levels.level3 -> InterventionLevel.RECOVER
            sessionSeconds >= levels.level2 -> InterventionLevel.REFLECT
            sessionSeconds >= levels.level1 -> InterventionLevel.GENTLE
            else -> InterventionLevel.NONE
        }

        val message = when (level) {
            InterventionLevel.GENTLE -> "You've been on $appName for $sessionDisplayMinutes minutes. Stay mindful."
            InterventionLevel.REFLECT -> "You've spent $sessionDisplayMinutes minutes on $appName. Let's pause and reflect."
            InterventionLevel.RECOVER -> "Time for a break? You've been on $appName for $sessionDisplayMinutes minutes."
            InterventionLevel.PROTECT -> "Time to step away from $appName. You've done enough for today."
            InterventionLevel.NONE -> ""
        }

        emit(InterventionDecision(
            shouldIntervene = level != InterventionLevel.NONE,
            level = level,
            message = message,
            appName = appName,
            sessionMinutes = sessionDisplayMinutes
        ))
    }
}
