package com.example.viora.domain.usecase.focus

import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFocusHistoryUseCase @Inject constructor(
    private val focusRepository: FocusRepository
) {
    operator fun invoke(limit: Int = 20): Flow<List<FocusSession>> {
        return focusRepository.getRecentSessions(limit)
    }
}
