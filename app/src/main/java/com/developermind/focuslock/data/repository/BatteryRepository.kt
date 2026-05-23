package com.developermind.focuslock.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.developermind.focuslock.data.model.BatteryState
import com.developermind.focuslock.util.BatteryMonitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryRepository(private val context: Context) {

    fun observeBatteryState(): Flow<BatteryState> = callbackFlow {
        trySend(BatteryMonitor.getCurrentState(context))

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                trySend(BatteryMonitor.getCurrentState(ctx))
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        awaitClose { context.unregisterReceiver(receiver) }
    }
}
