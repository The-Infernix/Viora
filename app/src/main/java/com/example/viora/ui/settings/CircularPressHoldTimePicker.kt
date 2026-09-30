package com.example.viora.ui.settings

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viora.ui.theme.InterventionGentle
import com.example.viora.ui.theme.InterventionNudge
import com.example.viora.ui.theme.InterventionPause
import com.example.viora.ui.theme.InterventionProtect
import com.example.viora.ui.theme.VioraPrimary
import kotlinx.coroutines.delay

@Composable
fun CircularPressHoldTimePicker(
    level: Int,
    label: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (level) {
        1 -> InterventionGentle
        2 -> InterventionNudge
        3 -> InterventionPause
        else -> InterventionProtect
    }

    val minVal = valueRange.first.toFloat()
    val maxVal = valueRange.last.toFloat()
    val totalSpan = maxVal - minVal

    var isPressed by remember { mutableStateOf(false) }
    var fillProgress by remember { mutableFloatStateOf((value - minVal) / totalSpan) }
    var lastHapticValue by remember { mutableFloatStateOf(value.toFloat()) }

    val animatedProgress by animateFloatAsState(
        targetValue = fillProgress,
        animationSpec = tween(durationMillis = 50),
        label = "fill"
    )

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    LaunchedEffect(isPressed) {
        if (!isPressed) return@LaunchedEffect
        while (isPressed && fillProgress < 1f) {
            fillProgress += (1f / 180f)
            if (fillProgress > 1f) fillProgress = 1f

            val currentValue = (minVal + totalSpan * fillProgress).toInt()
            if ((currentValue - lastHapticValue).toInt() >= 10) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                lastHapticValue = currentValue.toFloat()
            }
            delay(16)
        }
        if (isPressed) {
            val finalValue = maxVal.toInt()
            onValueChange(finalValue)
            isPressed = false
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(88.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        fillProgress = ((value.toFloat() - minVal).coerceIn(minVal, maxVal) - minVal) / totalSpan
                        lastHapticValue = value.toFloat()
                        waitForUpOrCancellation()
                        isPressed = false
                        val finalValue = (minVal + totalSpan * fillProgress).toInt().coerceIn(valueRange)
                        onValueChange(finalValue)
                    }
                }
        ) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val startAngle = -90f
            val sweepAngle = 360f * animatedProgress

            drawArc(
                color = color.copy(alpha = 0.12f),
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        val displayValue = if (isPressed) {
            (minVal + totalSpan * fillProgress).toInt()
        } else {
            value
        }
        Text(
            text = formatTime(displayValue),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = "$label · Level $level",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTime(seconds: Int): String {
    return if (seconds >= 60) {
        val m = seconds / 60
        val s = seconds % 60
        if (s == 0) "${m}m" else "${m}m ${s}s"
    } else {
        "${seconds}s"
    }
}
