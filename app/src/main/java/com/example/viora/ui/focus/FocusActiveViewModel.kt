package com.example.viora.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.model.FocusType
import com.example.viora.domain.repository.FocusRepository
import com.example.viora.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusActiveUiState(
    val focusType: FocusType = FocusType.DEEP_WORK,
    val totalSeconds: Int = 0,
    val timeRemaining: Int = 0,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val sessionId: Long = 0,
    val pausedAt: Int = 0
)

@HiltViewModel
class FocusActiveViewModel @Inject constructor(
    private val focusRepository: FocusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusActiveUiState())
    val uiState: StateFlow<FocusActiveUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initialize(focusType: String, durationMinutes: Int) {
        val type = try {
            FocusType.valueOf(focusType)
        } catch (_: Exception) {
            FocusType.DEEP_WORK
        }
        val totalSeconds = durationMinutes * 60

        _uiState.update {
            it.copy(
                focusType = type,
                totalSeconds = totalSeconds,
                timeRemaining = totalSeconds,
                isRunning = false,
                isComplete = false,
                sessionId = 0,
                pausedAt = 0
            )
        }
    }

    fun startSession() {
        viewModelScope.launch {
            val state = _uiState.value
            val session = FocusSession(
                type = state.focusType,
                startTime = System.currentTimeMillis(),
                plannedDurationMinutes = state.totalSeconds / 60,
                date = TimeUtils.todayString()
            )
            val sessionId = focusRepository.startSession(session)
            _uiState.update { it.copy(sessionId = sessionId, isRunning = true) }
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && _uiState.value.isRunning) {
                delay(1000)
                _uiState.update { it.copy(timeRemaining = it.timeRemaining - 1) }
            }
            if (_uiState.value.timeRemaining <= 0 && _uiState.value.isRunning) {
                _uiState.update { it.copy(isComplete = true, isRunning = false) }
                endSession(completed = true)
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun resume() {
        _uiState.update { it.copy(isRunning = true) }
        startTimer()
    }

    fun togglePauseResume() {
        if (_uiState.value.isRunning) pause() else resume()
    }

    fun endSession(completed: Boolean = false) {
        timerJob?.cancel()
        val state = _uiState.value
        val actualMinutes = (state.totalSeconds - state.timeRemaining) / 60

        viewModelScope.launch {
            if (state.sessionId > 0) {
                focusRepository.endSession(
                    id = state.sessionId,
                    actualMinutes = actualMinutes,
                    interruptedCount = 0,
                    completed = completed
                )
            }
            _uiState.update { it.copy(isRunning = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        if (!_uiState.value.isComplete && _uiState.value.sessionId > 0) {
            endSession(completed = false)
        }
    }
}
