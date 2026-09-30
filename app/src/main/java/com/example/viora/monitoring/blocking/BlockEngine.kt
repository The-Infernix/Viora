package com.example.viora.monitoring.blocking

import kotlinx.coroutines.flow.StateFlow

interface BlockEngine {
    val blockedPackages: StateFlow<Set<String>>

    fun block(packageName: String)
    fun unblock(packageName: String)
    fun isBlocked(packageName: String): Boolean
    fun getBlockedPackages(): Set<String>
    fun resetAll()
}
