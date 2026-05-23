package com.developermind.focuslock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Must be registered at runtime inside ScreenMonitorService — NOT in AndroidManifest.xml.
// ACTION_SCREEN_ON / ACTION_SCREEN_OFF / ACTION_USER_PRESENT are not on the
// implicit-broadcast exceptions list and are silently ignored when declared statically
// (Android 8.0+ background restrictions).
class ScreenReceiver(
    private val onScreenOn: () -> Unit,
    private val onScreenOff: () -> Unit,
    private val onUserPresent: () -> Unit,
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> onScreenOn()
            Intent.ACTION_SCREEN_OFF -> onScreenOff()
            Intent.ACTION_USER_PRESENT -> onUserPresent()
        }
    }
}
