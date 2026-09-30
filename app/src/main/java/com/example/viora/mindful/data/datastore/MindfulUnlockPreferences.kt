package com.example.viora.mindful.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mindfulDataStore: DataStore<Preferences> by preferencesDataStore(name = "mindful_unlock_preferences")

object MindfulUnlockKeys {
    val ENABLED = booleanPreferencesKey("mindful_unlock_enabled")
    val FREQUENCY = stringPreferencesKey("mindful_unlock_frequency")
    val FREQUENCY_THRESHOLD = intPreferencesKey("mindful_unlock_frequency_threshold")
    val CUSTOM_REASONS = stringSetPreferencesKey("mindful_unlock_custom_reasons")
    val HAPTICS_ENABLED = booleanPreferencesKey("mindful_unlock_haptics")
    val ANIMATION_SPEED = floatPreferencesKey("mindful_unlock_animation_speed")
    val SHOW_STATS = booleanPreferencesKey("mindful_unlock_show_stats")
    val CONSECUTIVE_JUST_CHECKING = intPreferencesKey("mindful_unlock_consecutive_jc")
}

@Singleton
class MindfulUnlockPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.mindfulDataStore

    val isEnabled: Flow<Boolean> = dataStore.data.map { it[MindfulUnlockKeys.ENABLED] ?: true }
    val frequency: Flow<String> = dataStore.data.map { it[MindfulUnlockKeys.FREQUENCY] ?: "OCCASIONAL" }
    val frequencyThreshold: Flow<Int> = dataStore.data.map { it[MindfulUnlockKeys.FREQUENCY_THRESHOLD] ?: 15 }
    val customReasons: Flow<Set<String>> = dataStore.data.map { it[MindfulUnlockKeys.CUSTOM_REASONS] ?: emptySet() }
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[MindfulUnlockKeys.HAPTICS_ENABLED] ?: true }
    val animationSpeed: Flow<Float> = dataStore.data.map { it[MindfulUnlockKeys.ANIMATION_SPEED] ?: 1f }
    val showStats: Flow<Boolean> = dataStore.data.map { it[MindfulUnlockKeys.SHOW_STATS] ?: true }
    val consecutiveJustChecking: Flow<Int> = dataStore.data.map { it[MindfulUnlockKeys.CONSECUTIVE_JUST_CHECKING] ?: 0 }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[MindfulUnlockKeys.ENABLED] = enabled }
    }

    suspend fun setFrequency(frequency: String) {
        dataStore.edit { it[MindfulUnlockKeys.FREQUENCY] = frequency }
    }

    suspend fun setFrequencyThreshold(threshold: Int) {
        dataStore.edit { it[MindfulUnlockKeys.FREQUENCY_THRESHOLD] = threshold }
    }

    suspend fun setCustomReasons(reasons: Set<String>) {
        dataStore.edit { it[MindfulUnlockKeys.CUSTOM_REASONS] = reasons }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[MindfulUnlockKeys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setAnimationSpeed(speed: Float) {
        dataStore.edit { it[MindfulUnlockKeys.ANIMATION_SPEED] = speed }
    }

    suspend fun setShowStats(show: Boolean) {
        dataStore.edit { it[MindfulUnlockKeys.SHOW_STATS] = show }
    }

    suspend fun setConsecutiveJustChecking(count: Int) {
        dataStore.edit { it[MindfulUnlockKeys.CONSECUTIVE_JUST_CHECKING] = count }
    }
}
