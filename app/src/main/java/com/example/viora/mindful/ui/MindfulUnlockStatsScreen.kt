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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.ui.components.common.VioraCard
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.ui.theme.VioraTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulUnlockStatsScreen(
    viewModel: MindfulUnlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Unlock Stats",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (uiState.isLoadingStats) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = VioraPrimary)
            }
        } else {
            val stats = uiState.stats

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
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Unlocks Today",
                            value = "${stats?.totalUnlocksToday ?: 0}",
                            icon = Icons.Outlined.Analytics
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Prompts Shown",
                            value = "${stats?.promptShownToday ?: 0}",
                            icon = Icons.Outlined.CheckCircle
                        )
                    }
                }

                item {
                    VioraCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Intentional vs Mindless",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val intentional = stats?.intentionalUnlocks ?: 0
                            val mindless = stats?.mindlessUnlocks ?: 0
                            val total = intentional + mindless

                            if (total > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Intentional: $intentional",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = VioraTertiary
                                    )
                                    Text(
                                        text = "Mindless: $mindless",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { intentional.toFloat() / total },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = VioraTertiary,
                                    trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                                )
                            } else {
                                Text(
                                    text = "No data yet today",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    VioraCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Just Checking Rate",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val jcPercent = ((stats?.justCheckingPercentage ?: 0f) * 100).toInt()
                            Text(
                                text = "${jcPercent}% of unlocks",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (jcPercent > 50) MaterialTheme.colorScheme.error else VioraTertiary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (jcPercent > 50) {
                                    "Most of your unlocks are unintentional. Consider reducing frequency."
                                } else {
                                    "Good job! Most of your unlocks are intentional."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    VioraCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Most Common Reason",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stats?.mostCommonReason ?: "No data yet",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = VioraPrimary
                            )
                        }
                    }
                }

                stats?.weeklyReport?.let { report ->
                    item {
                        VioraCard(
                            modifier = Modifier.fillMaxWidth(),
                            variant = com.example.viora.ui.components.common.CardVariant.LARGE
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Weekly Report",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                ReportRow("Avg. unlocks/day", String.format("%.1f", report.averageUnlocksPerDay))
                                ReportRow("Top reason", report.mostCommonReason)
                                ReportRow("Peak hour", "${report.peakHour}:00")
                                ReportRow("Prompts shown", "${report.totalPromptsShown}")
                                ReportRow("Lock again rate", "${(report.lockPhoneAgainRate * 100).toInt()}%")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VioraPrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
