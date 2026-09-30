package com.example.viora.domain.model

enum class InterventionLevel(val displayName: String, val seconds: Int) {
    NONE("None", 0),
    GENTLE("Gentle", 30),
    REFLECT("Reflect", 60),
    RECOVER("Recover", 120),
    PROTECT("Protect", 300);

    companion object {
        fun fromSeconds(seconds: Int): InterventionLevel {
            return entries.filter { it != NONE }.lastOrNull { seconds >= it.seconds } ?: NONE
        }
    }
}
