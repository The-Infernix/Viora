package com.example.viora.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viora.ui.theme.VioraPrimary

@Composable
fun WeeklyTrendChart(
    dailyTotals: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxMinutes = dailyTotals.maxOfOrNull { it.second / 60 }?.coerceAtLeast(1) ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Weekly Trend",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val barWidth = (size.width / (dailyTotals.size.coerceAtLeast(1) * 2f)).coerceAtMost(24.dp.toPx())
                val spacing = size.width / dailyTotals.size.coerceAtLeast(1)

                dailyTotals.forEachIndexed { index, (_, totalSeconds) ->
                    val barHeight = (totalSeconds.toFloat() / 60f / maxMinutes * size.height).coerceIn(0f, size.height)
                    val x = index * spacing + (spacing - barWidth) / 2
                    val y = size.height - barHeight

                    drawRoundRect(
                        color = VioraPrimary,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            if (dailyTotals.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    dailyTotals.takeLast(7).forEachIndexed { index, _ ->
                        Text(
                            text = labels.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
