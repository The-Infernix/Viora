package com.example.viora.ui.insights.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viora.domain.model.WeeklyReport
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.ui.theme.WellbeingHigh
import com.example.viora.ui.theme.WellbeingLow
import com.example.viora.ui.theme.WellbeingMedium
import com.example.viora.util.TimeUtils

@Composable
fun WeeklyReportCard(
    report: WeeklyReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Weekly Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${report.weekStartDate} — ${report.weekEndDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = report.grade.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = report.grade.label,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = VioraPrimary
                    )
                }
            }

            // Key metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricColumn(
                    label = "Avg Daily",
                    value = TimeUtils.formatDuration(report.avgDailyScreenTimeMinutes),
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    label = "Total",
                    value = TimeUtils.formatDuration(report.totalScreenTimeMinutes),
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    label = "Pick-ups",
                    value = "${report.avgPickupsPerDay}/day",
                    modifier = Modifier.weight(1f)
                )
            }

            // Focus & Goals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricColumn(
                    label = "Focus Sessions",
                    value = "${report.focusSessionsCompleted}",
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    label = "Goal Met",
                    value = "${report.goalMetDays}/7 days",
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    label = "Streak",
                    value = "${report.currentStreak} days",
                    modifier = Modifier.weight(1f)
                )
            }

            // Change from previous week
            if (report.changeFromPreviousWeek != 0) {
                val isImproved = report.changeFromPreviousWeek < 0
                Text(
                    text = if (isImproved) {
                        "↓ ${kotlin.math.abs(report.changeFromPreviousWeek)}% vs last week — great progress!"
                    } else {
                        "↑ ${report.changeFromPreviousWeek}% vs last week — room to improve"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = if (isImproved) WellbeingHigh else WellbeingMedium
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
