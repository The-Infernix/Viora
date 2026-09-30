package com.example.viora.mindful.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.mindful.domain.model.UnlockFrequency
import com.example.viora.mindful.domain.model.UnlockReason
import com.example.viora.mindful.domain.model.UnlockStats
import com.example.viora.mindful.domain.repository.MindfulUnlockRepository
import com.example.viora.mindful.domain.usecase.GetUnlockStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MindfulUnlockUiState(
    val isEnabled: Boolean = true,
    val frequency: UnlockFrequency = UnlockFrequency.OCCASIONAL,
    val frequencyThreshold: Int = 15,
    val customReasons: List<UnlockReason> = UnlockReason.DEFAULT_REASONS,
    val hapticsEnabled: Boolean = true,
    val animationSpeed: Float = 1f,
    val showStats: Boolean = true,
    val stats: UnlockStats? = null,
    val isLoadingStats: Boolean = false
)

@HiltViewModel
class MindfulUnlockViewModel @Inject constructor(
    private val repository: MindfulUnlockRepository,
    private val getUnlockStats: GetUnlockStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MindfulUnlockUiState())
    val uiState: StateFlow<MindfulUnlockUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.isEnabled().collect { _uiState.update { s -> s.copy(isEnabled = it) } }
        }
        viewModelScope.launch {
            repository.getFrequency().collect { _uiState.update { s -> s.copy(frequency = it) } }
        }
        viewModelScope.launch {
            repository.getFrequencyThreshold().collect { _uiState.update { s -> s.copy(frequencyThreshold = it) } }
        }
        viewModelScope.launch {
            repository.getCustomReasons().collect { _uiState.update { s -> s.copy(customReasons = it) } }
        }
        viewModelScope.launch {
            repository.getHapticsEnabled().collect { _uiState.update { s -> s.copy(hapticsEnabled = it) } }
        }
        viewModelScope.launch {
            repository.getAnimationSpeed().collect { _uiState.update { s -> s.copy(animationSpeed = it) } }
        }
        viewModelScope.launch {
            repository.getShowStats().collect { _uiState.update { s -> s.copy(showStats = it) } }
        }
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(enabled) }
    }

    fun setFrequency(frequency: UnlockFrequency) {
        viewModelScope.launch { repository.setFrequency(frequency) }
    }

    fun setFrequencyThreshold(threshold: Int) {
        viewModelScope.launch { repository.setFrequencyThreshold(threshold) }
    }

    fun setCustomReasons(reasons: List<UnlockReason>) {
        viewModelScope.launch { repository.setCustomReasons(reasons) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun setAnimationSpeed(speed: Float) {
        viewModelScope.launch { repository.setAnimationSpeed(speed) }
    }

    fun setShowStats(show: Boolean) {
        viewModelScope.launch { repository.setShowStats(show) }
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true) }
            try {
                val stats = getUnlockStats()
                _uiState.update { it.copy(stats = stats, isLoadingStats = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingStats = false) }
            }
        }
    }
}
