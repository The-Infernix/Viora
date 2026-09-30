package com.example.viora.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
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
import com.example.viora.domain.model.InterventionLevel
import com.example.viora.ui.overlay.InterventionOverlayContent
import com.example.viora.util.Logger

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentOverlay: ComposeView? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null

    fun showOverlay(
        level: InterventionLevel,
        appName: String,
        sessionMinutes: Int,
        goalMinutes: Int,
        onDismiss: () -> Unit,
        onExtend: () -> Unit,
        onStartFocus: () -> Unit,
        onCloseApp: () -> Unit
    ) {
        dismissOverlay()

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
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
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
                    InterventionOverlayContent(
                        level = level,
                        appName = appName,
                        sessionMinutes = sessionMinutes,
                        goalMinutes = goalMinutes,
                        onDismiss = {
                            dismissOverlay()
                            onDismiss()
                        },
                        onExtend = {
                            dismissOverlay()
                            onExtend()
                        },
                        onStartFocus = {
                            dismissOverlay()
                            onStartFocus()
                        },
                        onCloseApp = {
                            dismissOverlay()
                            onCloseApp()
                        }
                    )
                }
            }

            currentOverlay = composeView
            windowManager.addView(composeView, params)
            Logger.d("Overlay shown: level=$level, app=$appName, ${sessionMinutes}min")
        } catch (e: Exception) {
            Logger.e("Failed to show overlay: ${e.message}", e)
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
            Logger.e("Failed to dismiss overlay: ${e.message}")
        }
    }

    fun isShowing(): Boolean = currentOverlay?.isAttachedToWindow == true

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
