package com.example.viora.mindful.domain.usecase

import com.example.viora.mindful.domain.model.UnlockStats
import com.example.viora.mindful.domain.repository.MindfulUnlockRepository
import javax.inject.Inject

class GetUnlockStatsUseCase @Inject constructor(
    private val repository: MindfulUnlockRepository
) {
    suspend operator fun invoke(): UnlockStats {
        return repository.getUnlockStats()
    }
}
