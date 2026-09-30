package com.example.viora.ui.insights

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.domain.usecase.insights.InsightPeriod
import com.example.viora.ui.components.common.LoadingState
import com.example.viora.ui.components.common.VioraCard
import com.example.viora.ui.insights.components.SessionTimeline
import com.example.viora.ui.insights.components.UsageHeatmap
import com.example.viora.ui.insights.components.WeeklyReportCard
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
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
                    text = "Insights",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InsightPeriod.entries.forEach { period ->
                FilterChip(
                    selected = uiState.period == period,
                    onClick = { viewModel.loadInsights(period) },
                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VioraPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = VioraPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.insights?.let { insights ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InsightStatCard(
                                label = "Total",
                                value = TimeUtils.formatDuration(insights.totalScreenTimeMinutes),
                                modifier = Modifier.weight(1f)
                            )
                            InsightStatCard(
                                label = "Daily avg",
                                value = TimeUtils.formatDuration(insights.avgDailyMinutes),
                                modifier = Modifier.weight(1f)
                            )
                            InsightStatCard(
                                label = "Peak hour",
                                value = "${insights.peakHour}:00",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        VioraCard(
                            modifier = Modifier.fillMaxWidth(),
                            variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "App breakdown",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                insights.appBreakdown.forEach { app ->
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
                                    if (app != insights.appBreakdown.last()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (insights.sessionTimeline.isNotEmpty()) {
                        item {
                            SessionTimeline(
                                entries = insights.sessionTimeline,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    if (insights.heatmapData.isNotEmpty()) {
                        item {
                            UsageHeatmap(
                                data = insights.heatmapData,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                uiState.weeklyReport?.let { report ->
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        WeeklyReportCard(report = report)
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
private fun InsightStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    VioraCard(
        modifier = modifier,
        variant = com.example.viora.ui.components.common.CardVariant.MEDIUM
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
