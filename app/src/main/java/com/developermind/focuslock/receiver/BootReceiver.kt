package com.developermind.focuslock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.developermind.focuslock.service.ScreenMonitorService

// Relaunches ScreenMonitorService after the device reboots so the overlay
// is available without the user having to open the app manually.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(Intent(context, ScreenMonitorService::class.java))
        }
    }
}
