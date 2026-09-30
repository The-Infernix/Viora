package com.example.viora.domain.usecase.insights

import com.example.viora.domain.model.UsageHeatmapData
import com.example.viora.domain.repository.UsageRepository
import com.example.viora.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsageHeatmapUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    operator fun invoke(): Flow<List<UsageHeatmapData>> {
        val startDate = TimeUtils.lastNWeeksAgo(4)
        val endDate = TimeUtils.todayString()
        return usageRepository.getUsageHeatmap(startDate, endDate)
    }
}
