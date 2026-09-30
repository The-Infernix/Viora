package com.example.viora.data.source

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ForegroundAppInfo(
    val packageName: String,
    val timestamp: Long,
    val eventType: Int
)

@Singleton
class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private var lastEventTime = 0L

    fun detectForegroundApp(): ForegroundAppInfo? {
        val endTime = System.currentTimeMillis()
        val beginTime = maxOf(lastEventTime, endTime - 60_000)

        val events = usageStatsManager.queryEvents(beginTime, endTime) ?: return null
        val event = UsageEvents.Event()

        var foregroundPackage = ""
        var latestTimestamp = 0L
        var eventType = 0

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType in FOREGROUND_EVENTS && event.timeStamp > latestTimestamp) {
                latestTimestamp = event.timeStamp
                foregroundPackage = event.packageName
                eventType = event.eventType
            }
        }

        return if (foregroundPackage.isNotEmpty() && foregroundPackage !in IGNORED_PACKAGES) {
            lastEventTime = latestTimestamp
            ForegroundAppInfo(foregroundPackage, latestTimestamp, eventType)
        } else null
    }

    fun getUsageStatsForToday(): Map<String, Long> {
        val endTime = System.currentTimeMillis()
        val startTime = getStartOfDay()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        return stats?.associate { it.packageName to it.totalTimeInForeground } ?: emptyMap()
    }

    fun getPickupCount(): Int {
        val endTime = System.currentTimeMillis()
        val startTime = getStartOfDay()
        val events = usageStatsManager.queryEvents(startTime, endTime) ?: return 0
        val event = UsageEvents.Event()
        var count = 0
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                count++
            }
        }
        return count
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    companion object {
        private val FOREGROUND_EVENTS = setOf(
            UsageEvents.Event.MOVE_TO_FOREGROUND,
            UsageEvents.Event.ACTIVITY_RESUMED
        )

        private val IGNORED_PACKAGES = setOf(
            "android", "com.android.systemui", "com.android.launcher3",
            "com.android.settings", "com.example.viora", ""
        )
    }
}
