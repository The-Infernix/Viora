package com.example.viora.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.service.VioraTrackingService
import com.example.viora.ui.components.common.VioraCard
import com.example.viora.ui.theme.VioraPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToMindfulUnlock: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = "General", icon = Icons.Outlined.Speed) {
                    SettingsSliderItem(
                        title = "Daily goal",
                        value = uiState.dailyGoalMinutes.toFloat(),
                        valueRange = 30f..480f,
                        steps = 14,
                        displayValue = "${uiState.dailyGoalMinutes} min",
                        onValueChange = { viewModel.setDailyGoal(it.toInt()) }
                    )
                }
            }

            item {
                SettingsSection(title = "Focus", icon = Icons.Outlined.CenterFocusStrong) {
                    SettingsSliderItem(
                        title = "Default duration",
                        value = uiState.defaultFocusDuration.toFloat(),
                        valueRange = 5f..120f,
                        steps = 22,
                        displayValue = "${uiState.defaultFocusDuration} min",
                        onValueChange = { viewModel.setDefaultFocusDuration(it.toInt()) }
                    )
                }
            }

            item {
                SettingsSection(title = "Interventions", icon = Icons.Outlined.Alarm) {
                    SettingsSwitchItem(
                        title = "Enable interventions",
                        subtitle = "Show gentle reminders when you exceed limits",
                        checked = uiState.interventionsEnabled,
                        onCheckedChange = { viewModel.setInterventionsEnabled(it) }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CircularPressHoldTimePicker(
                            level = 1,
                            label = "Gentle",
                            value = uiState.level1Minutes,
                            valueRange = 10..120,
                            onValueChange = { viewModel.setInterventionLevels(it, uiState.level2Minutes, uiState.level3Minutes, uiState.level4Minutes) }
                        )
                        CircularPressHoldTimePicker(
                            level = 2,
                            label = "Reflect",
                            value = uiState.level2Minutes,
                            valueRange = 20..180,
                            onValueChange = { viewModel.setInterventionLevels(uiState.level1Minutes, it, uiState.level3Minutes, uiState.level4Minutes) }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CircularPressHoldTimePicker(
                            level = 3,
                            label = "Recover",
                            value = uiState.level3Minutes,
                            valueRange = 30..300,
                            onValueChange = { viewModel.setInterventionLevels(uiState.level1Minutes, uiState.level2Minutes, it, uiState.level4Minutes) }
                        )
                        CircularPressHoldTimePicker(
                            level = 4,
                            label = "Protect",
                            value = uiState.level4Minutes,
                            valueRange = 60..600,
                            onValueChange = { viewModel.setInterventionLevels(uiState.level1Minutes, uiState.level2Minutes, uiState.level3Minutes, it) }
                        )
                    }
                    SettingsSwitchItem(
                        title = "Intent check",
                        subtitle = "Ask why before opening distracting apps",
                        checked = uiState.intentCheckEnabled,
                        onCheckedChange = { viewModel.setIntentCheckEnabled(it) }
                    )
                    SettingsSwitchItem(
                        title = "Progressive friction",
                        subtitle = "Gradually increase resistance over time",
                        checked = uiState.progressiveFrictionEnabled,
                        onCheckedChange = { viewModel.setProgressiveFrictionEnabled(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Mindful Unlock", icon = Icons.Outlined.TouchApp) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMindfulUnlock() }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mindful Unlock Settings",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Configure unlock prompts & analytics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "\u203A",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SettingsSection(title = "Notifications", icon = Icons.Outlined.Notifications) {
                    SettingsSwitchItem(
                        title = "Notifications",
                        subtitle = "Enable tracking and intervention notifications",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Appearance", icon = Icons.Outlined.DarkMode) {
                    SettingsSwitchItem(
                        title = "Dark mode",
                        subtitle = "Follow system theme",
                        checked = uiState.themeMode == "system",
                        onCheckedChange = { viewModel.setThemeMode(if (it) "system" else "light") }
                    )
                }
            }

            item {
                SettingsSection(title = "Tracking", icon = Icons.Outlined.Security) {
                    SettingsSwitchItem(
                        title = "Start on boot",
                        subtitle = "Automatically start tracking when phone restarts",
                        checked = uiState.startOnBoot,
                        onCheckedChange = { viewModel.setStartOnBoot(it) }
                    )
                    var trackingActive by remember { mutableStateOf(false) }
                    SettingsSwitchItem(
                        title = "Tracking service",
                        subtitle = if (trackingActive) "Active — monitoring usage" else "Inactive — tap to start",
                        checked = trackingActive,
                        onCheckedChange = { enabled ->
                            trackingActive = enabled
                            if (enabled) {
                                val intent = Intent(context, VioraTrackingService::class.java)
                                context.startForegroundService(intent)
                            } else {
                                val intent = Intent(context, VioraTrackingService::class.java)
                                context.stopService(intent)
                            }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "About", icon = Icons.Outlined.Info) {
                    Text(
                        text = "Viora v2.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = VioraPrimary)
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = VioraPrimary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = VioraPrimary,
                activeTrackColor = VioraPrimary
            )
        )
    }
}
