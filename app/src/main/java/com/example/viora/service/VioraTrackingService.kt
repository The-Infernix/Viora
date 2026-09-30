package com.example.viora.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import com.example.viora.mindful.domain.usecase.ShouldShowMindfulUnlockUseCase
import com.example.viora.mindful.service.MindfulUnlockOverlayManager
import com.example.viora.data.source.UsageStatsDataSource
import com.example.viora.domain.model.InterventionLevel
import com.example.viora.domain.model.UsageEvent
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.domain.repository.UsageRepository
import com.example.viora.monitoring.blocking.BlockEngine
import com.example.viora.monitoring.notification.NotificationEngine
import com.example.viora.monitoring.notification.NotificationKind
import com.example.viora.util.AppCategoryDetector
import com.example.viora.util.AppInfoHelper
import com.example.viora.util.Constants
import com.example.viora.util.Logger
import com.example.viora.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VioraTrackingService : Service() {

    @Inject lateinit var usageStatsDataSource: UsageStatsDataSource
    @Inject lateinit var notificationEngine: NotificationEngine
    @Inject lateinit var usageRepository: UsageRepository
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var insightsRepository: com.example.viora.domain.repository.InsightsRepository
    @Inject lateinit var shouldShowMindfulUnlock: ShouldShowMindfulUnlockUseCase
    @Inject lateinit var mindfulOverlayManager: MindfulUnlockOverlayManager
    @Inject lateinit var blockEngine: BlockEngine

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingRunnable: Runnable? = null

    private var currentPackageName = ""
    private var sessionStartTime = 0L
    private var sessionElapsedSeconds = 0
    private var lastInterventionLevel = InterventionLevel.NONE
    private var overlayManager: OverlayManager? = null
    private var lastSummaryDate = ""
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        overlayManager = OverlayManager(this)
        startForeground(
            NotificationKind.FOREGROUND.id,
            notificationEngine.buildForegroundNotification("Monitoring...")
        )
        lastSummaryDate = TimeUtils.todayString()
        calculateDailySummary(TimeUtils.daysAgo(1))
        loadMonitoredPackages()
        startTracking()
        registerUnlockReceiver()
        Logger.d("VioraTrackingService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_EXTEND -> handleExtend()
            ACTION_DISMISS -> handleDismiss()
            ACTION_END_SESSION -> handleEndSession()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingRunnable?.let { handler.removeCallbacks(it) }
        overlayManager?.dismissOverlay()
        unregisterUnlockReceiver()
        serviceScope.cancel()
        Logger.d("VioraTrackingService destroyed")
    }

    private fun registerUnlockReceiver() {
        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    if (intent.action != Intent.ACTION_USER_PRESENT) return

                    val pm = ctx.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
                    if (pm?.isPowerSaveMode == true) {
                        Logger.d("Mindful Unlock: Skipping — battery saver active")
                        return
                    }

                    val pendingResult = goAsync()
                    val unlockTime = System.currentTimeMillis()
                    handler.postDelayed({
                        serviceScope.launch {
                            try {
                                val shouldShow = shouldShowMindfulUnlock()
                                if (shouldShow) {
                                    Logger.d("Mindful Unlock: Showing overlay")
                                    handler.post {
                                        mindfulOverlayManager.showOverlay()
                                    }
                                } else {
                                    Logger.d("Mindful Unlock: Skipping — conditions not met")
                                    mindfulOverlayManager.recordSkippedUnlock(unlockTime)
                                }
                            } catch (e: Exception) {
                                Logger.e("Mindful Unlock error: ${e.message}")
                            } finally {
                                pendingResult.finish()
                            }
                        }
                    }, 800L)
                }
            }
            unlockReceiver = receiver
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            registerReceiver(receiver, filter, RECEIVER_EXPORTED)
            Logger.d("Mindful Unlock: Dynamic receiver registered")
        } catch (e: Exception) {
            Logger.e("Mindful Unlock: Failed to register receiver: ${e.message}")
        }
    }

    private fun unregisterUnlockReceiver() {
        try {
            unlockReceiver?.let { unregisterReceiver(it) }
            unlockReceiver = null
        } catch (_: Exception) {}
    }

    private fun startTracking() {
        pollingRunnable = object : Runnable {
            override fun run() {
                detectAndTrack()
                handler.postDelayed(this, Constants.TRACKING_INTERVAL_MS)
            }
        }
        handler.post(pollingRunnable!!)
    }

    private fun detectAndTrack() {
        val today = TimeUtils.todayString()
        if (today != lastSummaryDate) {
            if (lastSummaryDate.isNotEmpty()) {
                calculateDailySummary(lastSummaryDate)
            }
            lastSummaryDate = today
        }

        val appInfo = usageStatsDataSource.detectForegroundApp() ?: return

        // Still in same app — increment elapsed
        if (appInfo.packageName == currentPackageName) {
            if (sessionStartTime > 0) {
                sessionElapsedSeconds += (Constants.TRACKING_INTERVAL_MS / 1000).toInt()
                checkIntervention()
            }
            return
        }

        // App changed — record previous session
        if (currentPackageName.isNotEmpty() && sessionStartTime > 0) {
            recordSessionEnd()
        }

        // Start new session
        currentPackageName = appInfo.packageName
        sessionStartTime = appInfo.timestamp
        sessionElapsedSeconds = 0
        lastInterventionLevel = InterventionLevel.NONE

        val appName = AppInfoHelper.getAppName(this, appInfo.packageName)

        if (isPackageMonitored(appInfo.packageName)) {
            notificationEngine.updateForegroundNotification("Tracking: $appName")
        }
    }

    private fun checkIntervention() {
        val sessionDisplayMinutes = sessionElapsedSeconds / 60

        serviceScope.launch {
            try {
                val enabled = settingsRepository.isInterventionsEnabled().first()
                if (!enabled) return@launch

                val levels = settingsRepository.getInterventionLevels().first()
                val goal = settingsRepository.getDailyGoal().first()

                val level = when {
                    sessionElapsedSeconds >= levels.level4 -> InterventionLevel.PROTECT
                    sessionElapsedSeconds >= levels.level3 -> InterventionLevel.RECOVER
                    sessionElapsedSeconds >= levels.level2 -> InterventionLevel.REFLECT
                    sessionElapsedSeconds >= levels.level1 -> InterventionLevel.GENTLE
                    else -> InterventionLevel.NONE
                }

                // Only trigger if level increased
                if (level != InterventionLevel.NONE && level.ordinal > lastInterventionLevel.ordinal) {
                    lastInterventionLevel = level
                    val appName = AppInfoHelper.getAppName(this@VioraTrackingService, currentPackageName)

                    // Show overlay on main thread
                    handler.post {
                        overlayManager?.showOverlay(
                            level = level,
                            appName = appName,
                            sessionMinutes = sessionDisplayMinutes,
                            goalMinutes = goal,
                            onDismiss = { handleDismiss() },
                            onExtend = { handleExtend() },
                            onStartFocus = { handleStartFocus() },
                            onCloseApp = { handleCloseApp() }
                        )
                    }

                    // Also send notification as backup
                    notificationEngine.showInterventionNotification(
                        title = getNotificationTitle(level),
                        content = "You've been on $appName for $sessionDisplayMinutes minutes."
                    )

                    Logger.d("Intervention triggered: $level at ${sessionElapsedSeconds}s")
                }
            } catch (e: Exception) {
                Logger.e("Intervention check failed: ${e.message}")
            }
        }
    }

    private fun recordSessionEnd() {
        if (sessionStartTime == 0L || currentPackageName.isEmpty()) return

        val durationSeconds = ((System.currentTimeMillis() - sessionStartTime) / 1000).toInt()
        if (durationSeconds < 5) return

        val appName = AppInfoHelper.getAppName(this, currentPackageName)
        val category = AppCategoryDetector.detect(currentPackageName)
        val calendar = java.util.Calendar.getInstance()

        val event = UsageEvent(
            packageName = currentPackageName,
            appName = appName,
            appCategory = category,
            startTime = sessionStartTime,
            endTime = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            date = TimeUtils.todayString(),
            hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            wasInterrupted = lastInterventionLevel != InterventionLevel.NONE,
            interventionLevel = lastInterventionLevel.ordinal
        )

        serviceScope.launch {
            usageRepository.recordEvent(event)
        }

        currentPackageName = ""
        sessionStartTime = 0L
        sessionElapsedSeconds = 0
        lastInterventionLevel = InterventionLevel.NONE
    }

    private fun handleExtend() {
        sessionElapsedSeconds = (sessionElapsedSeconds - 300).coerceAtLeast(0)
        lastInterventionLevel = InterventionLevel.GENTLE
        serviceScope.launch {
            try {
                val levels = settingsRepository.getInterventionLevels().first()
                Logger.d("Session extended by 5 minutes (elapsed: ${sessionElapsedSeconds}s, levels: $levels)")
            } catch (e: Exception) {
                Logger.e("Failed to read intervention levels: ${e.message}")
            }
        }
    }

    private fun handleDismiss() {
        overlayManager?.dismissOverlay()
        Logger.d("Overlay dismissed")
    }

    private fun handleEndSession() {
        overlayManager?.dismissOverlay()
        recordSessionEnd()
    }

    private fun handleStartFocus() {
        overlayManager?.dismissOverlay()
        val intent = Intent(this, com.example.viora.ui.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("navigate_to", "focus")
        }
        startActivity(intent)
    }

    private fun handleCloseApp() {
        overlayManager?.dismissOverlay()
        if (currentPackageName.isNotEmpty()) {
            blockEngine.block(currentPackageName)
        }
        recordSessionEnd()
    }

    private fun calculateDailySummary(date: String) {
        serviceScope.launch {
            try {
                insightsRepository.calculateAndSaveDailySummary(date)
                Logger.d("Daily summary calculated for $date")
            } catch (e: Exception) {
                Logger.e("Failed to calculate daily summary: ${e.message}")
            }
        }
    }

    private var monitoredPackages: Set<String> = emptySet()

    private fun loadMonitoredPackages() {
        serviceScope.launch {
            try {
                settingsRepository.getMonitoredPackages().collect { packages ->
                    monitoredPackages = packages.toSet()
                }
            } catch (e: Exception) {
                Logger.e("Failed to load monitored packages: ${e.message}")
            }
        }
    }

    private fun isPackageMonitored(packageName: String): Boolean {
        return monitoredPackages.contains(packageName)
    }

    private fun getNotificationTitle(level: InterventionLevel): String = when (level) {
        InterventionLevel.GENTLE -> "Gentle Reminder"
        InterventionLevel.REFLECT -> "Time to Reflect"
        InterventionLevel.RECOVER -> "Take a Break"
        InterventionLevel.PROTECT -> "Time to Step Away"
        InterventionLevel.NONE -> "Viora"
    }

    companion object {
        const val ACTION_EXTEND = "com.example.viora.EXTEND"
        const val ACTION_DISMISS = "com.example.viora.DISMISS"
        const val ACTION_END_SESSION = "com.example.viora.END_SESSION"
    }
}
