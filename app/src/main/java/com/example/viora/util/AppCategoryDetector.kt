package com.example.viora.util

import android.content.pm.PackageManager
import com.example.viora.domain.model.AppCategory

object AppCategoryDetector {
    private val socialApps = listOf(
        "instagram", "facebook", "twitter", "tiktok", "snapchat",
        "threads", "linkedin", "pinterest", "reddit"
    )
    private val entertainmentApps = listOf(
        "youtube", "netflix", "spotify", "twitch", "primevideo",
        "disney", "hulu", "hbomax", "apple.tv"
    )
    private val communicationApps = listOf(
        "whatsapp", "telegram", "signal", "messenger", "discord",
        "slack", "teams", "zoom", "skype"
    )
    private val productivityApps = listOf(
        "notion", "todoist", "trello", "asana", "evernote",
        "onenote", "docs.google", "sheets.google"
    )
    private val gamingApps = listOf(
        "pubg", "fortnite", "cod", "clash", "genshin",
        "minecraft", "roblox", "candy", "among"
    )

    fun detect(packageName: String): AppCategory {
        val lower = packageName.lowercase()
        return when {
            socialApps.any { lower.contains(it) } -> AppCategory.SOCIAL
            entertainmentApps.any { lower.contains(it) } -> AppCategory.ENTERTAINMENT
            communicationApps.any { lower.contains(it) } -> AppCategory.COMMUNICATION
            productivityApps.any { lower.contains(it) } -> AppCategory.PRODUCTIVITY
            gamingApps.any { lower.contains(it) } -> AppCategory.GAMES
            lower.contains("news") || lower.contains("magazine") -> AppCategory.NEWS
            lower.contains("shop") || lower.contains("amazon") || lower.contains("flipkart") -> AppCategory.SHOPPING
            else -> AppCategory.OTHER
        }
    }
}
