package com.example.viora.mindful.domain.model

enum class UnlockFrequency(val displayName: String) {
    ALWAYS("Always"),
    OCCASIONAL("Occasionally"),
    LATE_NIGHT_ONLY("Only Late Night"),
    FOCUS_MODE_ONLY("Only During Focus Mode"),
    AFTER_X_UNLOCKS("Only After X Unlocks"),
    NEVER("Never")
}
