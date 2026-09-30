package com.example.viora.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viora.domain.model.InterventionLevel
import com.example.viora.ui.theme.InterventionGentle
import com.example.viora.ui.theme.InterventionProtect
import com.example.viora.ui.theme.InterventionRecover
import com.example.viora.ui.theme.InterventionReflect
import com.example.viora.ui.theme.VioraPrimary
import kotlinx.coroutines.delay

@Composable
fun InterventionOverlayContent(
    level: InterventionLevel,
    appName: String,
    sessionMinutes: Int,
    goalMinutes: Int,
    onDismiss: () -> Unit,
    onExtend: () -> Unit,
    onStartFocus: () -> Unit,
    onCloseApp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = getBackgroundAlpha(level)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { /* consume clicks */ },
        contentAlignment = getContentAlignment(level)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
            exit = fadeOut()
        ) {
            when (level) {
                InterventionLevel.GENTLE -> GentleOverlay(
                    appName = appName,
                    sessionMinutes = sessionMinutes,
                    onDismiss = onDismiss
                )
                InterventionLevel.REFLECT -> NudgeOverlay(
                    appName = appName,
                    sessionMinutes = sessionMinutes,
                    goalMinutes = goalMinutes,
                    onDismiss = onDismiss,
                    onExtend = onExtend,
                    onStartFocus = onStartFocus
                )
                InterventionLevel.RECOVER -> PauseOverlay(
                    appName = appName,
                    sessionMinutes = sessionMinutes,
                    goalMinutes = goalMinutes,
                    onDismiss = onDismiss,
                    onStartFocus = onStartFocus,
                    onCloseApp = onCloseApp
                )
                InterventionLevel.PROTECT -> ProtectOverlay(
                    appName = appName,
                    onCloseApp = onCloseApp,
                    onStartFocus = onStartFocus
                )
                InterventionLevel.NONE -> {}
            }
        }
    }
}

@Composable
private fun GentleOverlay(
    appName: String,
    sessionMinutes: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 48.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Take a breath.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = InterventionGentle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You've been on $appName for $sessionMinutes minutes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(5000)
        onDismiss()
    }
}

@Composable
private fun NudgeOverlay(
    appName: String,
    sessionMinutes: Int,
    goalMinutes: Int,
    onDismiss: () -> Unit,
    onExtend: () -> Unit,
    onStartFocus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = InterventionReflect,
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "Let's pause for a moment.",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "You've been on $appName for $sessionMinutes minutes.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val remaining = (goalMinutes - sessionMinutes).coerceAtLeast(0)
            if (remaining > 0) {
                Text(
                    text = "$remaining minutes left under your daily goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VioraPrimary
                )
            } else {
                Text(
                    text = "You've reached your daily goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { (sessionMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (sessionMinutes > goalMinutes) MaterialTheme.colorScheme.error else VioraPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VioraPrimary)
            ) {
                Text("I'm done", color = MaterialTheme.colorScheme.onPrimary)
            }

            Button(
                onClick = onExtend,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("5 more minutes", color = MaterialTheme.colorScheme.onSurface)
            }

            TextButton(onClick = onStartFocus) {
                Text("Start a focus session instead")
            }
        }
    }
}

@Composable
private fun PauseOverlay(
    appName: String,
    sessionMinutes: Int,
    goalMinutes: Int,
    onDismiss: () -> Unit,
    onStartFocus: () -> Unit,
    onCloseApp: () -> Unit
) {
    var holdProgress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(durationMillis = 100),
        label = "holdProgress"
    )

    LaunchedEffect(isHolding) {
        if (isHolding && holdProgress < 1f) {
            while (holdProgress < 1f) {
                holdProgress += 0.02f
                delay(60)
            }
            onDismiss()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Time for a breather.",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = InterventionRecover
            )

            Text(
                text = "You've been on $appName for $sessionMinutes minutes.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Button(
                onClick = onStartFocus,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VioraPrimary)
            ) {
                Text("Focus session", color = MaterialTheme.colorScheme.onPrimary)
            }

            Button(
                onClick = onCloseApp,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Filled.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Close $appName", color = MaterialTheme.colorScheme.onSurface)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Hold to continue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { isHolding = true }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = animatedProgress)
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(InterventionRecover)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectOverlay(
    appName: String,
    onCloseApp: () -> Unit,
    onStartFocus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Time to step away.",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = InterventionProtect
            )

            Text(
                text = "You've spent enough time on $appName today.\nIt'll be here tomorrow.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onCloseApp,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InterventionProtect)
            ) {
                Text("Take a break", color = Color.White)
            }

            TextButton(onClick = onStartFocus) {
                Text("Start a focus session instead")
            }
        }
    }
}

private fun getBackgroundAlpha(level: InterventionLevel): Float = when (level) {
    InterventionLevel.GENTLE -> 0f
    InterventionLevel.REFLECT -> 0.4f
    InterventionLevel.RECOVER -> 0.6f
    InterventionLevel.PROTECT -> 0.8f
    InterventionLevel.NONE -> 0f
}

private fun getContentAlignment(level: InterventionLevel) = when (level) {
    InterventionLevel.GENTLE -> Alignment.TopCenter
    InterventionLevel.REFLECT -> Alignment.Center
    InterventionLevel.RECOVER -> Alignment.Center
    InterventionLevel.PROTECT -> Alignment.Center
    InterventionLevel.NONE -> Alignment.Center
}
