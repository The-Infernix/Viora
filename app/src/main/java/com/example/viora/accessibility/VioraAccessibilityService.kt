package com.example.viora.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.viora.domain.model.InterventionLevel
import com.example.viora.monitoring.blocking.BlockEngine
import com.example.viora.service.OverlayManager
import com.example.viora.util.AppInfoHelper
import com.example.viora.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VioraAccessibilityService : AccessibilityService() {

    @Inject lateinit var blockEngine: BlockEngine

    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlayManager: OverlayManager? = null
    private var currentOverlayPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        overlayManager = OverlayManager(this)
        Logger.d("VioraAccessibilityService connected — blocking enabled")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isEmpty()) return
        if (packageName == this@VioraAccessibilityService.packageName) return
        if (packageName == currentOverlayPackage) return
        if (!blockEngine.isBlocked(packageName)) return

        Logger.d("Accessibility: blocked app $packageName in foreground")

        currentOverlayPackage = packageName
        val appName = AppInfoHelper.getAppName(this, packageName)

        mainHandler.post {
            overlayManager?.showOverlay(
                level = InterventionLevel.PROTECT,
                appName = appName,
                sessionMinutes = 0,
                goalMinutes = 0,
                onDismiss = { navigateHome() },
                onExtend = { navigateHome() },
                onStartFocus = { navigateToFocus() },
                onCloseApp = { navigateHome() }
            )
        }
    }

    override fun onInterrupt() {
        Logger.d("VioraAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayManager?.dismissOverlay()
        overlayManager = null
        Logger.d("VioraAccessibilityService destroyed")
    }

    private fun navigateHome() {
        mainHandler.postDelayed({
            try {
                performGlobalAction(GLOBAL_ACTION_HOME)
            } catch (e: Exception) {
                Logger.e("Failed to perform GLOBAL_ACTION_HOME: ${e.message}")
            }
            currentOverlayPackage = null
        }, 150)
    }

    private fun navigateToFocus() {
        mainHandler.postDelayed({
            try {
                performGlobalAction(GLOBAL_ACTION_HOME)
            } catch (_: Exception) {}
            currentOverlayPackage = null
        }, 150)

        val intent = Intent(this, Class.forName("com.example.viora.ui.MainActivity")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("navigate_to", "focus")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Logger.e("Failed to launch focus: ${e.message}")
        }
    }
}
