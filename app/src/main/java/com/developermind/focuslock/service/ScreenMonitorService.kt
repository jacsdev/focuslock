package com.developermind.focuslock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.developermind.focuslock.R
import com.developermind.focuslock.receiver.ScreenReceiver
import com.developermind.focuslock.ui.overlay.OverlayActivity

class ScreenMonitorService : Service() {

    private val serviceChannelId = "focuslock_monitor"
    private val overlayChannelId = "focuslock_overlay"
    private lateinit var screenReceiver: ScreenReceiver

    override fun onCreate() {
        super.onCreate()
        createServiceNotificationChannel()
        createOverlayNotificationChannel()
        startForeground(SERVICE_NOTIFICATION_ID, buildServiceNotification())
        // Pre-launch while the app is in the foreground (MainActivity just started us).
        // OverlayActivity.onStart() will auto-hide if the phone is unlocked.
        // Having the activity already alive means the next REORDER_TO_FRONT
        // is instant and races ahead of the keyguard paint.
        OverlayActivity.launch(this)
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenReceiver() {
        screenReceiver = ScreenReceiver(
            onScreenOn = { showOverlayNotification() },
            onScreenOff = {},
            onUserPresent = { cancelOverlayNotification() },
        )
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    // Fires a high-priority full-screen intent notification — the only Android-approved
    // way to start an activity above the lock screen from a background service (API 29+
    // blocks startActivity() from background; USE_FULL_SCREEN_INTENT is the correct path).
    private fun showOverlayNotification() {
        val intent = Intent(this, OverlayActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_NO_ANIMATION,
            )
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, overlayChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.app_name))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setAutoCancel(true)
            .build()
        getSystemService(NotificationManager::class.java)
            .notify(OVERLAY_NOTIFICATION_ID, notification)
    }

    private fun cancelOverlayNotification() {
        getSystemService(NotificationManager::class.java)
            .cancel(OVERLAY_NOTIFICATION_ID)
    }

    private fun createServiceNotificationChannel() {
        val channel = NotificationChannel(
            serviceChannelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_MIN,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    // HIGH importance is required for full-screen intents to fire.
    // Sound and vibration are explicitly disabled — this is a silent trigger channel.
    private fun createOverlayNotificationChannel() {
        val channel = NotificationChannel(
            overlayChannelId,
            getString(R.string.notification_overlay_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildServiceNotification(): Notification =
        NotificationCompat.Builder(this, serviceChannelId)
            .setContentTitle(getString(R.string.notification_service_active))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

    companion object {
        private const val SERVICE_NOTIFICATION_ID = 1
        const val OVERLAY_NOTIFICATION_ID = 2
    }
}
