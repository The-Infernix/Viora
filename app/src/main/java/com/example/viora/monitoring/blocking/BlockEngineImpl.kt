package com.example.viora.monitoring.blocking

import android.os.Handler
import android.os.Looper
import com.example.viora.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockEngineImpl @Inject constructor() : BlockEngine {

    private val _blockedPackages = MutableStateFlow<Set<String>>(emptySet())
    override val blockedPackages: StateFlow<Set<String>> = _blockedPackages.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    init {
        scheduleMidnightReset()
    }

    override fun block(packageName: String) {
        _blockedPackages.value = _blockedPackages.value + packageName
        Logger.d("Blocked app: $packageName")
    }

    override fun unblock(packageName: String) {
        _blockedPackages.value = _blockedPackages.value - packageName
        Logger.d("Unblocked app: $packageName")
    }

    override fun isBlocked(packageName: String): Boolean {
        return packageName in _blockedPackages.value
    }

    override fun getBlockedPackages(): Set<String> = _blockedPackages.value

    override fun resetAll() {
        _blockedPackages.value = emptySet()
        Logger.d("All blocked apps reset (midnight)")
    }

    private fun scheduleMidnightReset() {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val delayMs = midnight.timeInMillis - now.timeInMillis

        handler.postDelayed({
            resetAll()
            scheduleMidnightReset()
        }, delayMs)
    }
}
