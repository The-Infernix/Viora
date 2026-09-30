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
import androidx.compose.ui.unit.sp
import com.example.viora.domain.model.UsageHeatmapData
import com.example.viora.ui.theme.VioraPrimary

@Composable
fun UsageHeatmap(
    data: List<UsageHeatmapData>,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val hourLabels = listOf("12a", "", "", "3a", "", "", "6a", "", "", "9a", "", "",
        "12p", "", "", "3p", "", "", "6p", "", "", "9p", "", "")

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Usage Heatmap",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Day labels column
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.width(20.dp)
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Heatmap grid
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp * 7 + 2.dp * 6)
                ) {
                    val cellWidth = (size.width / 24f)
                    val cellHeight = 18.dp.toPx()
                    val gap = 2.dp.toPx()
                    val cornerRadius = 3.dp.toPx()

                    // Build lookup map
                    val dataMap = data.associateBy { "${it.dayOfWeek}_${it.hourOfDay}" }

                    for (day in 0 until 7) {
                        for (hour in 0 until 24) {
                            val intensity = dataMap["${day}_${hour}"]?.intensity ?: 0f
                            val x = hour * (cellWidth + gap)
                            val y = day * (cellHeight + gap)

                            val color = when {
                                intensity == 0f -> Color.Gray.copy(alpha = 0.08f)
                                intensity < 0.25f -> VioraPrimary.copy(alpha = 0.2f)
                                intensity < 0.5f -> VioraPrimary.copy(alpha = 0.4f)
                                intensity < 0.75f -> VioraPrimary.copy(alpha = 0.65f)
                                else -> VioraPrimary.copy(alpha = 0.9f)
                            }

                            drawRoundRect(
                                color = color,
                                topLeft = Offset(x, y),
                                size = Size(cellWidth, cellHeight),
                                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Hour labels below heatmap
            Row(
                modifier = Modifier
                    .padding(start = 24.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                hourLabels.forEach { label ->
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf(0.08f, 0.2f, 0.4f, 0.65f, 0.9f).forEach { alpha ->
                    Spacer(modifier = Modifier.width(2.dp))
                    Canvas(modifier = Modifier.height(10.dp).width(10.dp)) {
                        drawRoundRect(
                            color = VioraPrimary.copy(alpha = alpha),
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
