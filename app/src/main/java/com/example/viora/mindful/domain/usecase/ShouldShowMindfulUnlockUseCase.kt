package com.example.viora.mindful.domain.usecase

import com.example.viora.mindful.domain.repository.MindfulUnlockRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ShouldShowMindfulUnlockUseCase @Inject constructor(
    private val repository: MindfulUnlockRepository
) {
    suspend operator fun invoke(): Boolean {
        if (!repository.isEnabled().first()) return false
        return repository.shouldShowPrompt()
    }
}
