package com.example.viora.domain.usecase.focus

import com.example.viora.domain.repository.FocusRepository
import javax.inject.Inject

class EndFocusSessionUseCase @Inject constructor(
    private val focusRepository: FocusRepository
) {
    suspend operator fun invoke(
        sessionId: Long,
        actualMinutes: Int,
        interruptedCount: Int,
        completed: Boolean
    ) {
        focusRepository.endSession(
            id = sessionId,
            actualMinutes = actualMinutes,
            interruptedCount = interruptedCount,
            completed = completed
        )
    }
}
