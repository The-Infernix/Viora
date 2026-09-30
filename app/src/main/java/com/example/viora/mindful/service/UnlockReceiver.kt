package com.example.viora.mindful.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.viora.mindful.domain.usecase.ShouldShowMindfulUnlockUseCase
import com.example.viora.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnlockReceiver : BroadcastReceiver() {

    @Inject lateinit var shouldShowMindfulUnlock: ShouldShowMindfulUnlockUseCase
    @Inject lateinit var overlayManager: MindfulUnlockOverlayManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_USER_PRESENT) return

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        if (powerManager?.isPowerSaveMode == true) {
            Logger.d("Mindful Unlock: Skipping — battery saver active")
            return
        }

        val pendingResult = goAsync()
        handler.postDelayed({
            scope.launch {
                try {
                    val shouldShow = shouldShowMindfulUnlock()
                    if (shouldShow) {
                        Logger.d("Mindful Unlock: Showing overlay")
                        handler.post {
                            overlayManager.showOverlay()
                        }
                    } else {
                        Logger.d("Mindful Unlock: Skipping — conditions not met")
                        overlayManager.recordSkippedUnlock(System.currentTimeMillis())
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
