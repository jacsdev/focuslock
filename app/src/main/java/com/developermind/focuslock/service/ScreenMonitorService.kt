package com.developermind.focuslock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.developermind.focuslock.R
import com.developermind.focuslock.receiver.ScreenReceiver
import com.developermind.focuslock.ui.overlay.OverlayActivity

class ScreenMonitorService : Service() {

    private val channelId = "focuslock_monitor"
    private lateinit var screenReceiver: ScreenReceiver

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        // Pre-launch the overlay so it is already resident in memory before
        // the first ACTION_SCREEN_ON fires — this is what makes REORDER_TO_FRONT
        // instant and eliminates the keyguard flash on subsequent screen-on events.
        OverlayActivity.launch(this)
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenReceiver() {
        screenReceiver = ScreenReceiver(
            onScreenOn = { OverlayActivity.launch(this) },
            onScreenOff = {},
            onUserPresent = {},
        )
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_MIN,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_service_active))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
