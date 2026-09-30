package com.example.viora.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.viora.data.datastore.UserPreferencesDataStore
import com.example.viora.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var preferences: UserPreferencesDataStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val startOnBoot = runBlocking { preferences.startOnBoot.first() }
        if (startOnBoot) {
            Logger.d("Boot completed, starting tracking service")
            val serviceIntent = Intent(context, VioraTrackingService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}
