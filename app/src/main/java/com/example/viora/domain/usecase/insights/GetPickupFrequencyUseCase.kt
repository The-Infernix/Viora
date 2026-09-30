package com.example.viora.domain.usecase.insights

import com.example.viora.domain.repository.UsageRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPickupFrequencyUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    operator fun invoke(): Flow<List<Pair<String, Int>>> {
        val startDate = TimeUtils.lastNDays(7).first()
        val endDate = TimeUtils.todayString()
        return usageRepository.getDailyTotals(startDate, endDate)
    }
}
