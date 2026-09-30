package com.example.viora.mindful.domain.repository

import com.example.viora.mindful.domain.model.UnlockEvent
import com.example.viora.mindful.domain.model.UnlockFrequency
import com.example.viora.mindful.domain.model.UnlockReason
import com.example.viora.mindful.domain.model.UnlockStats
import kotlinx.coroutines.flow.Flow

interface MindfulUnlockRepository {
    fun isEnabled(): Flow<Boolean>
    fun getFrequency(): Flow<UnlockFrequency>
    fun getFrequencyThreshold(): Flow<Int>
    fun getCustomReasons(): Flow<List<UnlockReason>>
    fun getHapticsEnabled(): Flow<Boolean>
    fun getAnimationSpeed(): Flow<Float>
    fun getShowStats(): Flow<Boolean>
    fun isReasonCustomized(): Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)
    suspend fun setFrequency(frequency: UnlockFrequency)
    suspend fun setFrequencyThreshold(threshold: Int)
    suspend fun setCustomReasons(reasons: List<UnlockReason>)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setAnimationSpeed(speed: Float)
    suspend fun setShowStats(show: Boolean)

    suspend fun getTodayUnlockCount(): Int
    suspend fun getTodayPromptedCount(): Int
    suspend fun getConsecutiveJustChecking(): Int
    suspend fun recordUnlockEvent(event: UnlockEvent)
    suspend fun incrementConsecutiveJustChecking()
    suspend fun resetConsecutiveJustChecking()
    suspend fun getUnlockStats(): UnlockStats
    suspend fun shouldShowPrompt(): Boolean
    suspend fun cleanupOldData(olderThanDays: Int)
}
