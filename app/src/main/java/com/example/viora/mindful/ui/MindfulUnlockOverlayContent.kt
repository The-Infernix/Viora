package com.example.viora.mindful.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viora.mindful.domain.model.UnlockReason
import kotlinx.coroutines.delay

private val GlassSurface = Color(0x0FFFFFFF)
private val GlassBorder = Color(0x1AFFFFFF)
private val GlassSurfaceSelected = Color(0x29FFFFFF)
private val GlassBorderSelected = Color(0x4DFFFFFF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MindfulUnlockOverlayContent(
    onReasonSelected: (reasonId: String, reasonLabel: String, wasJustChecking: Boolean) -> Unit,
    onLockPhone: () -> Unit,
    onPauseFor30Seconds: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf<UnlockReason?>(null) }
    var showNudge by remember { mutableStateOf(false) }
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        entered = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
        )

        AnimatedVisibility(
            visible = !showNudge,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(200))
        ) {
            ReasonSelectionScreen(
                entered = entered,
                selectedReason = selectedReason,
                onChipSelected = { reason ->
                    selectedReason = reason
                    if (reason.id == "just_checking") {
                        showNudge = true
                    }
                },
                onContinue = {
                    selectedReason?.let {
                        onReasonSelected(it.id, it.label, false)
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = showNudge,
            enter = fadeIn(tween(300, 100)) + slideInVertically(tween(400, 100)) { it / 5 },
            exit = fadeOut(tween(200))
        ) {
            NudgeScreen(
                onContinue = { onReasonSelected("just_checking", "Just Checking", true) },
                onLockPhone = onLockPhone,
                onPause = onPauseFor30Seconds
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReasonSelectionScreen(
    entered: Boolean,
    selectedReason: UnlockReason?,
    onChipSelected: (UnlockReason) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        AnimatedVisibility(
            visible = entered,
            enter = fadeIn(tween(800))
        ) {
            BreathingOrb()
        }

        Spacer(modifier = Modifier.height(40.dp))

        HeadlineSection(entered = entered)

        Spacer(modifier = Modifier.height(40.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            UnlockReason.DEFAULT_REASONS.forEachIndexed { index, reason ->
                StaggeredChip(
                    reason = reason,
                    index = index,
                    entered = entered,
                    isSelected = reason.id == selectedReason?.id,
                    onClick = { onChipSelected(reason) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = selectedReason != null,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)
            ) { it } + fadeIn(tween(300)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(150))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Continue" },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun BreathingOrb() {
    val scaleAnimatable = remember { Animatable(0.85f) }
    val alphaAnimatable = remember { Animatable(0.2f) }
    val easing = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)

    LaunchedEffect(Unit) {
        while (true) {
            scaleAnimatable.animateTo(1.15f, tween(4000, easing = easing))
            scaleAnimatable.animateTo(0.85f, tween(4000, easing = easing))
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            alphaAnimatable.animateTo(0.5f, tween(4000, easing = easing))
            alphaAnimatable.animateTo(0.2f, tween(4000, easing = easing))
        }
    }

    val scale = scaleAnimatable.value
    val alpha = alphaAnimatable.value
    val accent = MaterialTheme.colorScheme.primary

    val density = LocalDensity.current
    val outerRadius = with(density) { 40.dp.toPx() }
    val innerRadius = with(density) { 24.dp.toPx() }
    val coreRadius = with(density) { 8.dp.toPx() }

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha * 0.3f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.4f), Color.Transparent),
                        radius = outerRadius
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha * 0.6f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.6f), Color.Transparent),
                        radius = innerRadius
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha.coerceAtMost(0.8f)
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent, accent.copy(alpha = 0f)),
                        radius = coreRadius
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun HeadlineSection(entered: Boolean) {
    AnimatedVisibility(
        visible = entered,
        enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
            animationSpec = tween(600, delayMillis = 200, easing = FastOutSlowInEasing)
        ) { it / 4 }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "What brings",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "you here?",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Take a moment to set your intention",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StaggeredChip(
    reason: UnlockReason,
    index: Int,
    entered: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(entered) {
        if (entered) {
            delay(400L + index * 50L)
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250)) + slideInVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) { it / 3 }
    ) {
        ReasonChip(
            reason = reason,
            isSelected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun ReasonChip(
    reason: UnlockReason,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = when {
        isSelected -> 1.03f
        isPressed -> 0.96f
        else -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "chip_scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) GlassBorderSelected else GlassBorder,
        animationSpec = tween(200),
        label = "chip_border"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) GlassSurfaceSelected else GlassSurface,
        animationSpec = tween(200),
        label = "chip_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(200),
        label = "chip_text"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(50.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .semantics {
                contentDescription = "${reason.label}${if (isSelected) ", selected" else ""}"
            }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getReasonEmoji(reason.icon),
                fontSize = 16.sp
            )
            Text(
                text = reason.label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun NudgeScreen(
    onContinue: () -> Unit,
    onLockPhone: () -> Unit,
    onPause: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Before you\ncontinue...",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Light,
                lineHeight = 44.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Take a breath. You're reaching for your\nphone without a specific goal.",
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp
            ),
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "That's okay \u2014 awareness is the first step.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 26.sp
            ),
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Continue anyway",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPause,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White.copy(alpha = 0.8f)
            ),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Text(
                text = "Take a 30-second pause",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onLockPhone,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Lock my phone",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getReasonEmoji(icon: String): String = when (icon) {
    "chat" -> "\uD83D\uDCAC"
    "notifications" -> "\uD83D\uDD14"
    "work" -> "\uD83D\uDCBC"
    "study" -> "\uD83D\uDCDA"
    "call" -> "\uD83D\uDCDE"
    "music" -> "\uD83C\uDFB5"
    "camera" -> "\uD83D\uDCF7"
    "navigation" -> "\uD83D\uDDFA"
    "shopping" -> "\uD83D\uDED2"
    "entertainment" -> "\uD83C\uDFAC"
    "social" -> "\uD83D\uDC65"
    "check" -> "\uD83D\uDC4D"
    "other" -> "\u2728"
    else -> "\u2728"
}
