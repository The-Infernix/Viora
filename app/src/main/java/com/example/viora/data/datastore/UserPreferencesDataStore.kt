package com.example.viora.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "viora_preferences")

object PreferenceKeys {
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
    val INTERVENTIONS_ENABLED = booleanPreferencesKey("interventions_enabled")
    val LEVEL_1_MINUTES = intPreferencesKey("level_1_minutes")
    val LEVEL_2_MINUTES = intPreferencesKey("level_2_minutes")
    val LEVEL_3_MINUTES = intPreferencesKey("level_3_minutes")
    val LEVEL_4_MINUTES = intPreferencesKey("level_4_minutes")
    val INTENT_CHECK_ENABLED = booleanPreferencesKey("intent_check_enabled")
    val PROGRESSIVE_FRICTION_ENABLED = booleanPreferencesKey("progressive_friction_enabled")
    val DEFAULT_FOCUS_DURATION = intPreferencesKey("default_focus_duration")
    val AMBIENT_SOUND_ENABLED = booleanPreferencesKey("ambient_sound_enabled")
    val SELECTED_AMBIENT_SOUND = stringPreferencesKey("selected_ambient_sound")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ACCENT_COLOR = intPreferencesKey("accent_color")
    val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
    val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")
    val WEEKLY_REPORT_ENABLED = booleanPreferencesKey("weekly_report_enabled")
    val MONITORED_PACKAGES = stringSetPreferencesKey("monitored_packages")
    val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
    val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
}

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.ONBOARDING_COMPLETE] ?: false }
    val dailyGoalMinutes: Flow<Int> = dataStore.data.map { it[PreferenceKeys.DAILY_GOAL_MINUTES] ?: 120 }
    val interventionsEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.INTERVENTIONS_ENABLED] ?: true }
    val level1Minutes: Flow<Int> = dataStore.data.map { it[PreferenceKeys.LEVEL_1_MINUTES] ?: 30 }
    val level2Minutes: Flow<Int> = dataStore.data.map { it[PreferenceKeys.LEVEL_2_MINUTES] ?: 60 }
    val level3Minutes: Flow<Int> = dataStore.data.map { it[PreferenceKeys.LEVEL_3_MINUTES] ?: 120 }
    val level4Minutes: Flow<Int> = dataStore.data.map { it[PreferenceKeys.LEVEL_4_MINUTES] ?: 180 }
    val intentCheckEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.INTENT_CHECK_ENABLED] ?: true }
    val progressiveFrictionEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.PROGRESSIVE_FRICTION_ENABLED] ?: true }
    val defaultFocusDuration: Flow<Int> = dataStore.data.map { it[PreferenceKeys.DEFAULT_FOCUS_DURATION] ?: 25 }
    val ambientSoundEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.AMBIENT_SOUND_ENABLED] ?: false }
    val selectedAmbientSound: Flow<String> = dataStore.data.map { it[PreferenceKeys.SELECTED_AMBIENT_SOUND] ?: "rain" }
    val themeMode: Flow<String> = dataStore.data.map { it[PreferenceKeys.THEME_MODE] ?: "system" }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true }
    val quietHoursStart: Flow<Int> = dataStore.data.map { it[PreferenceKeys.QUIET_HOURS_START] ?: 22 }
    val quietHoursEnd: Flow<Int> = dataStore.data.map { it[PreferenceKeys.QUIET_HOURS_END] ?: 7 }
    val weeklyReportEnabled: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.WEEKLY_REPORT_ENABLED] ?: true }
    val monitoredPackages: Flow<Set<String>> = dataStore.data.map { it[PreferenceKeys.MONITORED_PACKAGES] ?: emptySet() }
    val startOnBoot: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.START_ON_BOOT] ?: true }
    val reducedMotion: Flow<Boolean> = dataStore.data.map { it[PreferenceKeys.REDUCED_MOTION] ?: false }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[PreferenceKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setDailyGoal(minutes: Int) {
        dataStore.edit { it[PreferenceKeys.DAILY_GOAL_MINUTES] = minutes }
    }

    suspend fun setInterventionsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.INTERVENTIONS_ENABLED] = enabled }
    }

    suspend fun setInterventionLevels(l1: Int, l2: Int, l3: Int, l4: Int) {
        dataStore.edit {
            it[PreferenceKeys.LEVEL_1_MINUTES] = l1
            it[PreferenceKeys.LEVEL_2_MINUTES] = l2
            it[PreferenceKeys.LEVEL_3_MINUTES] = l3
            it[PreferenceKeys.LEVEL_4_MINUTES] = l4
        }
    }

    suspend fun setIntentCheckEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.INTENT_CHECK_ENABLED] = enabled }
    }

    suspend fun setProgressiveFrictionEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.PROGRESSIVE_FRICTION_ENABLED] = enabled }
    }

    suspend fun setDefaultFocusDuration(minutes: Int) {
        dataStore.edit { it[PreferenceKeys.DEFAULT_FOCUS_DURATION] = minutes }
    }

    suspend fun setAmbientSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.AMBIENT_SOUND_ENABLED] = enabled }
    }

    suspend fun setSelectedAmbientSound(sound: String) {
        dataStore.edit { it[PreferenceKeys.SELECTED_AMBIENT_SOUND] = sound }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[PreferenceKeys.THEME_MODE] = mode }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setQuietHours(start: Int, end: Int) {
        dataStore.edit {
            it[PreferenceKeys.QUIET_HOURS_START] = start
            it[PreferenceKeys.QUIET_HOURS_END] = end
        }
    }

    suspend fun setWeeklyReportEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.WEEKLY_REPORT_ENABLED] = enabled }
    }

    suspend fun setMonitoredPackages(packages: Set<String>) {
        dataStore.edit { it[PreferenceKeys.MONITORED_PACKAGES] = packages }
    }

    suspend fun addMonitoredPackage(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.MONITORED_PACKAGES] ?: emptySet()
            prefs[PreferenceKeys.MONITORED_PACKAGES] = current + packageName
        }
    }

    suspend fun removeMonitoredPackage(packageName: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferenceKeys.MONITORED_PACKAGES] ?: emptySet()
            prefs[PreferenceKeys.MONITORED_PACKAGES] = current - packageName
        }
    }

    suspend fun setStartOnBoot(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.START_ON_BOOT] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.REDUCED_MOTION] = enabled }
    }
}
