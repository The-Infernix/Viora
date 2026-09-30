package com.example.viora.ui

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.service.VioraTrackingService
import com.example.viora.ui.navigation.Screen
import com.example.viora.ui.navigation.VioraBottomNavBar
import com.example.viora.ui.navigation.VioraNavHost
import com.example.viora.ui.theme.VioraPrimary
import com.example.viora.ui.theme.VioraTheme
import com.example.viora.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var serviceRunning = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Logger.d("Notification permission granted: $granted")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        requestNotificationPermission()

        setContent {
            val themeMode by settingsRepository.getThemeMode().collectAsState(initial = "system")

            VioraTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    var startRoute by remember { mutableStateOf(Screen.Dashboard.route) }
                    var showPermissionGate by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        settingsRepository.isOnboardingComplete().collect { complete ->
                            startRoute = if (complete) Screen.Dashboard.route
                            else Screen.Onboarding.route
                            showPermissionGate = complete
                        }
                    }

                    val hasUsageStats = remember { mutableStateOf(hasUsageStatsPermission()) }
                    val hasOverlay = remember { mutableStateOf(hasOverlayPermission()) }

                    // Recheck when coming back from settings
                    val lifecycleOwner = LocalLifecycleOwner.current
                    LaunchedEffect(lifecycleOwner) {
                        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                            override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event) {
                                if (event == Lifecycle.Event.ON_RESUME) {
                                    hasUsageStats.value = hasUsageStatsPermission()
                                    hasOverlay.value = hasOverlayPermission()
                                }
                            }
                        })
                    }

                    val allPermissionsGranted = hasUsageStats.value && hasOverlay.value

                    // Auto-start tracking service when onboarding is done and permissions are granted
                    LaunchedEffect(allPermissionsGranted, startRoute) {
                        if (startRoute == Screen.Dashboard.route && allPermissionsGranted) {
                            try {
                                val intent = Intent(this@MainActivity, VioraTrackingService::class.java)
                                startForegroundService(intent)
                                Logger.d("Tracking service auto-started")
                            } catch (e: Exception) {
                                Logger.e("Failed to start tracking service: ${e.message}")
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Permission gate
                        AnimatedVisibility(
                            visible = showPermissionGate && !allPermissionsGranted,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            PermissionGate(
                                hasUsageStats = hasUsageStats.value,
                                hasOverlay = hasOverlay.value,
                                onRequestUsageStats = { openUsageStatsSettings() },
                                onRequestOverlay = { openOverlaySettings() },
                                onSkip = { showPermissionGate = false }
                            )
                        }

                        // Main content
                        if (!showPermissionGate || allPermissionsGranted) {
                            androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                VioraNavHost(
                                    navController = navController,
                                    onOnboardingComplete = { dailyGoalMinutes ->
                                        lifecycleScope.launch {
                                            settingsRepository.setDailyGoal(dailyGoalMinutes)
                                            settingsRepository.setOnboardingComplete(true)
                                        }
                                    },
                                    startDestination = startRoute
                                )
                                VioraBottomNavBar(
                                    navController = navController,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else true
    }

    private fun openUsageStatsSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun PermissionGate(
    hasUsageStats: Boolean,
    hasOverlay: Boolean,
    onRequestUsageStats: () -> Unit,
    onRequestOverlay: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .padding(top = 64.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Viora needs these permissions to track your screen time and show mindful interventions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Usage Stats
        PermissionRow(
            label = "Usage Access",
            description = "Read app usage data",
            granted = hasUsageStats,
            onRequest = onRequestUsageStats
        )

        // Overlay
        PermissionRow(
            label = "Display Over Apps",
            description = "Show intervention overlays",
            granted = hasOverlay,
            onRequest = onRequestOverlay
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = "Skip for now",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (granted) VioraPrimary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!granted) {
            Button(onClick = onRequest) {
                Text("Grant")
            }
        } else {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Granted",
                tint = VioraPrimary
            )
        }
    }
}
