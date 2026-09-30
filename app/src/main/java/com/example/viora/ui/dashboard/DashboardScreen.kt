package com.example.viora.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.ui.components.common.VioraCard
import com.example.viora.ui.theme.AccentGreen
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.ui.theme.WellbeingGreat
import com.example.viora.ui.theme.WellbeingFair
import com.example.viora.ui.theme.WellbeingCritical
import com.example.viora.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToFocus: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = uiState.greeting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        val data = uiState.dashboardData

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroTimeCard(
                    screenTimeMinutes = data?.screenTimeMinutes ?: 0,
                    goalMinutes = data?.dailyGoalMinutes ?: 120
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatItem(
                        value = "${data?.currentStreak ?: 0}",
                        label = "Day streak",
                        icon = Icons.Filled.LocalFireDepartment,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatItem(
                        value = TimeUtils.formatMinutes(data?.timeSavedMinutes ?: 0),
                        label = "Time saved",
                        icon = Icons.Outlined.Timelapse,
                        color = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatItem(
                        value = "${data?.todaysPickups ?: 0}",
                        label = "Pick-ups",
                        icon = Icons.Outlined.Notifications,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TopAppsCard(
                    apps = data?.topApps?.take(3) ?: emptyList()
                )
            }

            item {
                WeeklySparkline(
                    dailyTotals = data?.weeklyTotals ?: emptyList()
                )
            }

            item {
                InsightCard(
                    message = getInsightMessage(
                        screenTime = data?.screenTimeMinutes ?: 0,
                        goal = data?.dailyGoalMinutes ?: 120,
                        streak = data?.currentStreak ?: 0
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeroTimeCard(
    screenTimeMinutes: Int,
    goalMinutes: Int
) {
    val progress = if (goalMinutes > 0) {
        (screenTimeMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val remaining = (goalMinutes - screenTimeMinutes).coerceAtLeast(0)
    val isOverGoal = screenTimeMinutes > goalMinutes

    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.LARGE
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = Color.Gray.copy(alpha = 0.1f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val sweepAngle = 360f * animatedProgress
                    val arcColor = when {
                        isOverGoal -> WellbeingCritical
                        progress > 0.8f -> WellbeingFair
                        else -> WellbeingGreat
                    }
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatTime(screenTimeMinutes),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Screen time today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    isOverGoal -> WellbeingCritical
                    progress > 0.8f -> WellbeingFair
                    else -> WellbeingGreat
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (remaining > 0) {
                    "${TimeUtils.formatMinutes(remaining)} remaining under your goal"
                } else {
                    "Goal exceeded by ${TimeUtils.formatMinutes(-remaining)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverGoal) WellbeingCritical else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniStatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    VioraCard(
        modifier = modifier,
        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopAppsCard(
    apps: List<com.example.viora.domain.model.AppUsage>
) {
    if (apps.isEmpty()) return

    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Most used today",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            apps.forEach { app ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${app.durationMinutes}m",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (app != apps.last()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklySparkline(
    dailyTotals: List<Pair<String, Int>>
) {
    if (dailyTotals.isEmpty()) return

    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            val maxTotal = dailyTotals.maxOfOrNull { it.second } ?: 1
            val barColor = VioraPrimary

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyTotals.forEach { (date, total) ->
                    val heightFraction = total.toFloat() / maxTotal
                    val dayLabel = try {
                        val cal = java.util.Calendar.getInstance()
                        cal.time = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(date) ?: java.util.Date()
                        cal.getDisplayName(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SHORT, java.util.Locale.US) ?: ""
                    } catch (_: Exception) {
                        ""
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(20.dp)
                                .height((60 * heightFraction).dp.coerceAtLeast(4.dp))
                        ) {
                            drawRoundRect(
                                color = barColor,
                                cornerRadius = CornerRadius(4.dp.toPx()),
                                size = size
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    message: String
) {
    VioraCard(
        modifier = Modifier.fillMaxWidth(),
        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💡",
                    fontSize = 16.sp
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 -> "${hours}h ${mins}m"
        else -> "${mins}m"
    }
}

private fun getInsightMessage(screenTime: Int, goal: Int, streak: Int): String {
    return when {
        screenTime <= goal * 0.5 -> "You're having a great digital day! Your screen time is well under your goal."
        screenTime <= goal -> "You're on track today. Keep it up and maintain your streak!"
        screenTime <= goal * 1.2 -> "You're slightly over your goal. Consider taking a break."
        else -> "You've exceeded your goal. Time for a digital detox?"
    }
}
