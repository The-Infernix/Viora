package com.example.viora.ui.insights.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viora.domain.model.SessionTimelineEntry
import com.example.viora.ui.theme.AccentBlue
import com.example.viora.ui.theme.AccentGreen
import com.example.viora.ui.theme.AccentOrange
import com.example.viora.ui.theme.AccentPink
import com.example.viora.ui.theme.AccentPurple
import com.example.viora.ui.theme.AccentRed
import com.example.viora.ui.theme.AccentYellow

@Composable
fun SessionTimeline(
    entries: List<SessionTimelineEntry>,
    modifier: Modifier = Modifier
) {
    val totalMinutes = entries.sumOf { it.durationMinutes }.coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Session Timeline",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline bar
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                val barHeight = 24.dp.toPx()
                val cornerRadius = 6.dp.toPx()
                val totalWidth = size.width
                var xOffset = 0f

                entries.forEach { entry ->
                    val segmentWidth = (entry.durationMinutes.toFloat() / totalMinutes * totalWidth)
                        .coerceAtLeast(8.dp.toPx())
                    val color = getCategoryColor(entry.category.name)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(xOffset, (size.height - barHeight) / 2),
                        size = Size(segmentWidth - 2.dp.toPx(), barHeight),
                        cornerRadius = CornerRadius(cornerRadius)
                    )

                    xOffset += segmentWidth
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            val grouped = entries.groupBy { it.appName }
            grouped.entries.take(5).forEach { (appName, sessions) ->
                val totalMin = sessions.sumOf { it.durationMinutes }
                val category = sessions.first().category.name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(modifier = Modifier.height(10.dp).width(10.dp)) {
                            drawRoundRect(
                                color = getCategoryColor(category),
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height),
                                cornerRadius = CornerRadius(2.dp.toPx())
                            )
                        }
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "${totalMin}m",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getCategoryColor(category: String): Color = when (category) {
    "SOCIAL" -> AccentPink
    "ENTERTAINMENT" -> AccentOrange
    "PRODUCTIVITY" -> AccentGreen
    "GAMES" -> AccentRed
    "NEWS" -> AccentYellow
    "COMMUNICATION" -> AccentBlue
    "UTILITIES" -> AccentPurple
    else -> Color.Gray
}
