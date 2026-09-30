package com.example.viora.ui.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viora.ui.components.CircularTimer
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.ui.theme.WellbeingHigh

@Composable
fun FocusActiveScreen(
    focusType: String,
    durationMinutes: Int,
    onFinished: () -> Unit,
    viewModel: FocusActiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(focusType, durationMinutes) {
        viewModel.initialize(focusType, durationMinutes)
    }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = uiState.focusType.displayName,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        CircularTimer(
            timeRemainingSeconds = uiState.timeRemaining,
            totalTimeSeconds = uiState.totalSeconds,
            modifier = Modifier.height(240.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (uiState.isComplete) {
            Text(
                text = "Session Complete!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = WellbeingHigh
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Great job staying focused.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = VioraPrimary)
            ) {
                Text("Done", color = MaterialTheme.colorScheme.onPrimary)
            }
        } else {
            if (!uiState.isRunning && uiState.timeRemaining == uiState.totalSeconds) {
                Button(
                    onClick = { viewModel.startSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = VioraPrimary)
                ) {
                    Text("Start", color = MaterialTheme.colorScheme.onPrimary)
                }
            } else {
                Button(
                    onClick = { viewModel.togglePauseResume() },
                    colors = ButtonDefaults.buttonColors(containerColor = VioraPrimary)
                ) {
                    Text(
                        text = if (uiState.isRunning) "Pause" else "Resume",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.endSession(completed = false)
                    onFinished()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "End Session",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
