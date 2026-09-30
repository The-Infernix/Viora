package com.example.viora.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.domain.model.FocusSession
import com.example.viora.domain.model.FocusType
import com.example.viora.ui.components.common.VioraButton
import com.example.viora.ui.components.common.VioraCard
import com.example.viora.ui.components.common.ButtonVariant
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusHubScreen(
    onStartFocus: (String, Int) -> Unit = { _, _ -> },
    viewModel: FocusViewModel = hiltViewModel()
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
                    text = "Focus",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FocusStatChip(
                        label = "Today",
                        value = "${uiState.todaySessionCount} sessions",
                        modifier = Modifier.weight(1f)
                    )
                    FocusStatChip(
                        label = "This week",
                        value = "${uiState.weekSessionCount} sessions",
                        modifier = Modifier.weight(1f)
                    )
                    FocusStatChip(
                        label = "Total",
                        value = "${uiState.weekTotalMinutes} min",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                FocusTypeCard(
                    title = "Deep Work",
                    subtitle = "25 minutes of uninterrupted focus",
                    emoji = "🧠",
                    duration = 25,
                    onStart = { onStartFocus("DEEP_WORK", 25) }
                )
            }

            item {
                FocusTypeCard(
                    title = "Pomodoro",
                    subtitle = "25 min focus / 5 min break",
                    emoji = "🍅",
                    duration = 25,
                    onStart = { onStartFocus("POMODORO", 25) }
                )
            }

            item {
                FocusTypeCard(
                    title = "Quick Focus",
                    subtitle = "15 minutes of focused time",
                    emoji = "⚡",
                    duration = 15,
                    onStart = { onStartFocus("QUICK_FOCUS", 15) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (uiState.recentSessions.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent sessions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(uiState.recentSessions.take(5)) { session ->
                    SessionHistoryItem(session = session)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FocusTypeCard(
    title: String,
    subtitle: String,
    emoji: String,
    duration: Int,
    onStart: () -> Unit
) {
    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.LARGE,
        onClick = onStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryItem(session: FocusSession) {
    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.SMALL
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (session.completed) VioraPrimary else MaterialTheme.colorScheme.error)
                )
                Column {
                    Text(
                        text = session.type.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${session.actualDurationMinutes ?: session.plannedDurationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (session.completed) "Completed" else "Interrupted",
                style = MaterialTheme.typography.labelMedium,
                color = if (session.completed) VioraPrimary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun FocusStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    VioraCard(
        modifier = modifier,
        variant = com.example.viora.ui.components.common.CardVariant.SMALL
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
