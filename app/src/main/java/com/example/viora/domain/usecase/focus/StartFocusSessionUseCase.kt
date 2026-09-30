package com.example.viora.domain.usecase.focus

import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.model.FocusType
import com.example.viora.domain.repository.FocusRepository
import com.example.viora.util.TimeUtils
import javax.inject.Inject

class StartFocusSessionUseCase @Inject constructor(
    private val focusRepository: FocusRepository
) {
    suspend operator fun invoke(
        type: FocusType,
        durationMinutes: Int,
        ambientSound: String? = null
    ): Long {
        val session = FocusSession(
            type = type,
            startTime = System.currentTimeMillis(),
            plannedDurationMinutes = durationMinutes,
            ambientSound = ambientSound,
            date = TimeUtils.todayString()
        )
        return focusRepository.startSession(session)
    }
}
