package com.example.viora.mindful.domain.model

data class UnlockEvent(
    val id: Long = 0,
    val timestamp: Long,
    val date: String,
    val hourOfDay: Int,
    val selectedReasonId: String,
    val selectedReasonLabel: String,
    val wasJustChecking: Boolean,
    val lockedPhoneAgain: Boolean,
    val sessionLengthSeconds: Int,
    val openedSocialMediaAfter: Boolean,
    val promptShown: Boolean,
    val promptSkipped: Boolean = false
)
