package com.developermind.focuslock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.developermind.focuslock.R
import com.developermind.focuslock.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Companion foreground service whose sole purpose is to keep the app process alive
 * at foreground priority. This prevents aggressive OEM killers (MIUI, One UI, etc.)
 * from terminating the process that hosts FocusLockAccessibilityService.
 *
 * This is intentionally separate from the AccessibilityService — calling
 * startForeground() from an AccessibilityService on API 34+ requires specialUse/
 * systemExempted type and hits a 6h/day cap with dataSync, both of which break
 * service restarts in different ways. A plain companion Service has none of those
 * constraints.
 */
class KeepAliveService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var observeJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()
        promote(enabled = true)
        observeEnabledState()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun observeEnabledState() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            PreferencesRepository(this@KeepAliveService)
                .observePreferences()
                .map { it.isEnabled }
                .distinctUntilChanged()
                .collect { enabled ->
                    getSystemService(NotificationManager::class.java)
                        .notify(NOTIFICATION_ID, buildNotification(enabled))
                }
        }
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { setShowBadge(false) }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun promote(enabled: Boolean) {
        val notification = buildNotification(enabled)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(enabled: Boolean): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(
                getString(
                    if (enabled) R.string.notification_service_active
                    else R.string.notification_service_paused,
                )
            )
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    companion object {
        private const val CHANNEL_ID = "keep_alive"
        const val NOTIFICATION_ID = 1001
    }
}
