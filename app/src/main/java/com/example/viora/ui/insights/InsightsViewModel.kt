package com.example.viora.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.domain.model.WeeklyReport
import com.example.viora.domain.usecase.insights.GetUsageInsightsUseCase
import com.example.viora.domain.usecase.insights.GetWeeklyReportUseCase
import com.example.viora.domain.usecase.insights.InsightPeriod
import com.example.viora.domain.usecase.insights.UsageInsights
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val isLoading: Boolean = true,
    val period: InsightPeriod = InsightPeriod.WEEK,
    val insights: UsageInsights? = null,
    val weeklyReport: WeeklyReport? = null,
    val error: String? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getUsageInsightsUseCase: GetUsageInsightsUseCase,
    private val getWeeklyReportUseCase: GetWeeklyReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights(InsightPeriod.WEEK)
    }

    fun loadInsights(period: InsightPeriod) {
        _uiState.update { it.copy(period = period, isLoading = true) }

        viewModelScope.launch {
            getUsageInsightsUseCase(period)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { insights ->
                    _uiState.update {
                        it.copy(isLoading = false, insights = insights, error = null)
                    }
                }
        }

        viewModelScope.launch {
            getWeeklyReportUseCase()
                .catch { }
                .collect { report ->
                    _uiState.update { it.copy(weeklyReport = report) }
                }
        }
    }
}
