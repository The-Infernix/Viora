package com.example.viora.mindful.domain.usecase

import com.example.viora.mindful.domain.model.UnlockEvent
import com.example.viora.mindful.domain.repository.MindfulUnlockRepository
import com.example.viora.util.TimeUtils
import javax.inject.Inject

class RecordUnlockEventUseCase @Inject constructor(
    private val repository: MindfulUnlockRepository
) {
    suspend operator fun invoke(
        reasonId: String,
        reasonLabel: String,
        wasJustChecking: Boolean,
        lockedPhoneAgain: Boolean,
        sessionLengthSeconds: Int,
        openedSocialMediaAfter: Boolean,
        promptShown: Boolean,
        promptSkipped: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()

        val event = UnlockEvent(
            timestamp = now,
            date = TimeUtils.todayString(),
            hourOfDay = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            selectedReasonId = reasonId,
            selectedReasonLabel = reasonLabel,
            wasJustChecking = wasJustChecking,
            lockedPhoneAgain = lockedPhoneAgain,
            sessionLengthSeconds = sessionLengthSeconds,
            openedSocialMediaAfter = openedSocialMediaAfter,
            promptShown = promptShown,
            promptSkipped = promptSkipped
        )

        repository.recordUnlockEvent(event)

        if (wasJustChecking) {
            repository.incrementConsecutiveJustChecking()
        } else {
            repository.resetConsecutiveJustChecking()
        }
    }
}
