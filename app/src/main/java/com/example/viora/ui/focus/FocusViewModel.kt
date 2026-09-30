package com.example.viora.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.usecase.focus.GetFocusHistoryUseCase
import com.example.viora.domain.repository.FocusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class FocusHubUiState(
    val isLoading: Boolean = true,
    val recentSessions: List<FocusSession> = emptyList(),
    val activeSession: FocusSession? = null,
    val weekSessionCount: Int = 0,
    val weekTotalMinutes: Int = 0,
    val todaySessionCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val getFocusHistoryUseCase: GetFocusHistoryUseCase,
    private val focusRepository: FocusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusHubUiState())
    val uiState: StateFlow<FocusHubUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
        loadStats()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            getFocusHistoryUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { sessions ->
                    _uiState.update {
                        it.copy(isLoading = false, recentSessions = sessions)
                    }
                }
        }

        viewModelScope.launch {
            focusRepository.getActiveSession()
                .collect { session ->
                    _uiState.update { it.copy(activeSession = session) }
                }
        }
    }

    private fun loadStats() {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        viewModelScope.launch {
            combine(
                focusRepository.getCompletedSessionsCount(
                    weekStart.format(formatter),
                    today.format(formatter)
                ),
                focusRepository.getTotalFocusMinutes(
                    weekStart.format(formatter),
                    today.format(formatter)
                ),
                focusRepository.getSessionsForDate(today.format(formatter))
            ) { weekCount, weekMinutes, todaySessions ->
                Triple(weekCount, weekMinutes, todaySessions.size)
            }.catch { /* ignore stat errors */ }
                .collect { (weekCount, weekMinutes, todayCount) ->
                    _uiState.update {
                        it.copy(
                            weekSessionCount = weekCount,
                            weekTotalMinutes = weekMinutes,
                            todaySessionCount = todayCount
                        )
                    }
                }
        }
    }
}
