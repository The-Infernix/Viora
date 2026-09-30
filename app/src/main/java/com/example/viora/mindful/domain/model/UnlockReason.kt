package com.example.viora.mindful.domain.model

data class UnlockReason(
    val id: String,
    val label: String,
    val icon: String,
    val isBuiltIn: Boolean = true
) {
    companion object {
        val DEFAULT_REASONS = listOf(
            UnlockReason("work", "Work", "work"),
            UnlockReason("study", "Study", "study"),
            UnlockReason("reply_messages", "Messages", "chat"),
            UnlockReason("call", "Call", "call"),
            UnlockReason("navigation", "Maps", "navigation"),
            UnlockReason("social_media", "Social Media", "social"),
            UnlockReason("just_checking", "Just Checking", "check"),
            UnlockReason("other", "Other", "other")
        )
    }
}
