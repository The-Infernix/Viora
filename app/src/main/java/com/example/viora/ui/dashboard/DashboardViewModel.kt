package com.example.viora.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.domain.model.WeeklyReport
import com.example.viora.domain.usecase.dashboard.DashboardData
import com.example.viora.domain.usecase.dashboard.GetDashboardUseCase
import com.example.viora.domain.usecase.dashboard.CalculateStreakUseCase
import com.example.viora.domain.usecase.insights.GetWeeklyReportUseCase
import com.example.viora.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val greeting: String = TimeUtils.getGreeting(),
    val dashboardData: DashboardData? = null,
    val weeklyReport: WeeklyReport? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardUseCase: GetDashboardUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val getWeeklyReportUseCase: GetWeeklyReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getDashboardUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            dashboardData = data,
                            error = null
                        )
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
