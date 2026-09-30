package com.example.viora.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viora.ui.theme.VioraPrimary

@Composable
fun CircularTimer(
    timeRemainingSeconds: Int,
    totalTimeSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTimeSeconds > 0) {
        timeRemainingSeconds.toFloat() / totalTimeSeconds
    } else 0f

    var animProgress by remember { mutableFloatStateOf(1f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animProgress,
        animationSpec = tween(1000),
        label = "timerAnim"
    )

    LaunchedEffect(timeRemainingSeconds) {
        animProgress = progress
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier) {
            val stroke = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            val arcSize = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx())
            val topLeft = Offset(8.dp.toPx(), 8.dp.toPx())

            drawArc(
                color = Color.Gray.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            drawArc(
                color = VioraPrimary,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val minutes = timeRemainingSeconds / 60
            val seconds = timeRemainingSeconds % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
