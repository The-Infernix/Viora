package com.example.viora.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viora.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyGoalMinutes: Int = 120,
    val interventionsEnabled: Boolean = true,
    val level1Minutes: Int = 30,
    val level2Minutes: Int = 60,
    val level3Minutes: Int = 120,
    val level4Minutes: Int = 180,
    val intentCheckEnabled: Boolean = true,
    val progressiveFrictionEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "system",
    val defaultFocusDuration: Int = 25,
    val startOnBoot: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getDailyGoal().collect { _uiState.update { s -> s.copy(dailyGoalMinutes = it) } }
        }
        viewModelScope.launch {
            settingsRepository.isInterventionsEnabled().collect { _uiState.update { s -> s.copy(interventionsEnabled = it) } }
        }
        viewModelScope.launch {
            settingsRepository.getInterventionLevels().collect { levels ->
                _uiState.update { s -> s.copy(level1Minutes = levels.level1, level2Minutes = levels.level2, level3Minutes = levels.level3, level4Minutes = levels.level4) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isIntentCheckEnabled().collect { _uiState.update { s -> s.copy(intentCheckEnabled = it) } }
        }
        viewModelScope.launch {
            settingsRepository.isProgressiveFrictionEnabled().collect { _uiState.update { s -> s.copy(progressiveFrictionEnabled = it) } }
        }
        viewModelScope.launch {
            settingsRepository.isNotificationsEnabled().collect { _uiState.update { s -> s.copy(notificationsEnabled = it) } }
        }
        viewModelScope.launch {
            settingsRepository.getThemeMode().collect { _uiState.update { s -> s.copy(themeMode = it) } }
        }
        viewModelScope.launch {
            settingsRepository.getDefaultFocusDuration().collect { _uiState.update { s -> s.copy(defaultFocusDuration = it) } }
        }
        viewModelScope.launch {
            settingsRepository.getStartOnBoot().collect { _uiState.update { s -> s.copy(startOnBoot = it) } }
        }
    }

    fun setDailyGoal(minutes: Int) {
        viewModelScope.launch { settingsRepository.setDailyGoal(minutes) }
    }

    fun setInterventionsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setInterventionsEnabled(enabled) }
    }

    fun setInterventionLevels(l1: Int, l2: Int, l3: Int, l4: Int) {
        viewModelScope.launch { settingsRepository.setInterventionLevels(l1, l2, l3, l4) }
    }

    fun setIntentCheckEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setIntentCheckEnabled(enabled) }
    }

    fun setProgressiveFrictionEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setProgressiveFrictionEnabled(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setDefaultFocusDuration(minutes: Int) {
        viewModelScope.launch { settingsRepository.setDefaultFocusDuration(minutes) }
    }

    fun setStartOnBoot(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setStartOnBoot(enabled) }
    }
}
