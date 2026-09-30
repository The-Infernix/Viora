package com.example.viora.mindful.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.mindful.domain.model.UnlockFrequency
import com.example.viora.ui.components.common.VioraCard
import com.example.viora.ui.theme.VioraPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulUnlockSettingsScreen(
    viewModel: MindfulUnlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Mindful Unlock",
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
                    SettingsSwitchItem(
                        title = "Enable Mindful Unlock",
                        subtitle = "Ask why you unlocked your phone",
                        checked = uiState.isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "Frequency", icon = Icons.Outlined.Tune) {
                    UnlockFrequency.values().forEach { freq ->
                        FrequencyRadioItem(
                            label = freq.displayName,
                            selected = uiState.frequency == freq,
                            onClick = { viewModel.setFrequency(freq) }
                        )
                    }

                    if (uiState.frequency == UnlockFrequency.AFTER_X_UNLOCKS) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ThresholdSlider(
                            value = uiState.frequencyThreshold.toFloat(),
                            onValueChange = { viewModel.setFrequencyThreshold(it.toInt()) },
                            valueRange = 5f..50f,
                            displayValue = "After ${uiState.frequencyThreshold} unlocks"
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Experience", icon = Icons.Outlined.Animation) {
                    SettingsSwitchItem(
                        title = "Enable Haptics",
                        subtitle = "Vibrate on interaction",
                        checked = uiState.hapticsEnabled,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) }
                    )

                    ThresholdSlider(
                        value = uiState.animationSpeed,
                        onValueChange = { viewModel.setAnimationSpeed(it) },
                        valueRange = 0.5f..2f,
                        displayValue = "Animation: ${String.format("%.1f", uiState.animationSpeed)}x"
                    )

                    SettingsSwitchItem(
                        title = "Show Statistics",
                        subtitle = "Display unlock analytics in Insights",
                        checked = uiState.showStats,
                        onCheckedChange = { viewModel.setShowStats(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "About", icon = Icons.Outlined.Policy) {
                    Text(
                        text = "Mindful Unlock helps you become more intentional about phone usage by gently asking why you unlocked your phone.",
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
private fun FrequencyRadioItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.RadioButton(
            selected = selected,
            onClick = onClick,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = VioraPrimary
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThresholdSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Threshold",
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
            colors = SliderDefaults.colors(
                thumbColor = VioraPrimary,
                activeTrackColor = VioraPrimary
            )
        )
    }
}
