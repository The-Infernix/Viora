package com.example.viora.mindful.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.viora.mindful.domain.usecase.RecordUnlockEventUseCase
import com.example.viora.mindful.ui.MindfulUnlockOverlayContent
import com.example.viora.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MindfulUnlockOverlayManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordUnlockEvent: RecordUnlockEventUseCase
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentOverlay: ComposeView? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var unlockTimestamp: Long = 0L

    fun showOverlay() {
        dismissOverlay()
        unlockTimestamp = System.currentTimeMillis()

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                PixelFormat.TRANSLUCENT
            )

            val lifecycleOwner = OverlayLifecycleOwner()
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            overlayLifecycleOwner = lifecycleOwner

            val composeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                setContent {
                    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        dynamicDarkColorScheme(context)
                    } else {
                        darkColorScheme()
                    }

                    MaterialTheme(colorScheme = colorScheme) {
                        MindfulUnlockOverlayContent(
                            onReasonSelected = { reasonId, reasonLabel, wasJustChecking ->
                                mainHandler.post { handleReasonSelected(reasonId, reasonLabel, wasJustChecking) }
                            },
                            onLockPhone = {
                                mainHandler.post {
                                    dismissOverlay()
                                    lockPhone()
                                }
                            },
                            onPauseFor30Seconds = {
                                mainHandler.post {
                                    dismissOverlay()
                                    pauseAndReRecord()
                                }
                            },
                            onDismiss = {
                                mainHandler.post { dismissOverlay() }
                            }
                        )
                    }
                }
            }

            currentOverlay = composeView
            windowManager.addView(composeView, params)
            Logger.d("Mindful Unlock overlay shown")
        } catch (e: Exception) {
            Logger.e("Failed to show mindful unlock overlay: ${e.message}")
        }
    }

    fun dismissOverlay() {
        try {
            currentOverlay?.let { view ->
                if (view.isAttachedToWindow) {
                    windowManager.removeViewImmediate(view)
                }
            }
            currentOverlay = null

            overlayLifecycleOwner?.let { owner ->
                owner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                owner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                owner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
            overlayLifecycleOwner = null
        } catch (e: Exception) {
            Logger.e("Failed to dismiss mindful unlock overlay: ${e.message}")
        }
    }

    fun recordSkippedUnlock(unlockTime: Long = System.currentTimeMillis()) {
        scope.launch {
            try {
                val sessionLength = ((System.currentTimeMillis() - unlockTime) / 1000).toInt()
                recordUnlockEvent(
                    reasonId = "skipped",
                    reasonLabel = "Skipped",
                    wasJustChecking = false,
                    lockedPhoneAgain = false,
                    sessionLengthSeconds = sessionLength,
                    openedSocialMediaAfter = false,
                    promptShown = false,
                    promptSkipped = true
                )
            } catch (e: Exception) {
                Logger.e("Failed to record skipped unlock: ${e.message}")
            }
        }
    }

    private fun handleReasonSelected(reasonId: String, reasonLabel: String, wasJustChecking: Boolean) {
        dismissOverlay()
        scope.launch {
            try {
                val sessionLength = ((System.currentTimeMillis() - unlockTimestamp) / 1000).toInt()
                recordUnlockEvent(
                    reasonId = reasonId,
                    reasonLabel = reasonLabel,
                    wasJustChecking = wasJustChecking,
                    lockedPhoneAgain = false,
                    sessionLengthSeconds = sessionLength,
                    openedSocialMediaAfter = false,
                    promptShown = true
                )
            } catch (e: Exception) {
                Logger.e("Failed to record unlock event: ${e.message}")
            }
        }
    }

    private fun pauseAndReRecord() {
        scope.launch {
            try {
                val sessionLength = ((System.currentTimeMillis() - unlockTimestamp) / 1000).toInt()
                recordUnlockEvent(
                    reasonId = "paused",
                    reasonLabel = "Paused 30s",
                    wasJustChecking = false,
                    lockedPhoneAgain = false,
                    sessionLengthSeconds = sessionLength,
                    openedSocialMediaAfter = false,
                    promptShown = true
                )
            } catch (e: Exception) {
                Logger.e("Failed to record pause event: ${e.message}")
            }
        }
    }

    private fun lockPhone() {
        scope.launch {
            try {
                val sessionLength = ((System.currentTimeMillis() - unlockTimestamp) / 1000).toInt()
                recordUnlockEvent(
                    reasonId = "locked",
                    reasonLabel = "Locked Phone",
                    wasJustChecking = false,
                    lockedPhoneAgain = true,
                    sessionLengthSeconds = sessionLength,
                    openedSocialMediaAfter = false,
                    promptShown = true
                )
            } catch (e: Exception) {
                Logger.e("Failed to record lock event: ${e.message}")
            }
        }
    }

    private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        init {
            savedStateRegistryController.performRestore(null)
        }

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }
}
