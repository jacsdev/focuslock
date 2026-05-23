package com.developermind.focuslock.ui.overlay

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.developermind.focuslock.service.ScreenMonitorService
import com.developermind.focuslock.ui.theme.FocusLockTheme

class OverlayActivity : ComponentActivity() {

    private val viewModel: OverlayViewModel by viewModels()

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) moveTaskToBack(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLockScreenFlags()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        hideSystemBars()

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            FocusLockTheme {
                OverlayScreen(
                    uiState = uiState.value,
                    onDismiss = { moveTaskToBack(false) },
                )
            }
        }
    }

    // Called when the singleTask instance is reused via REORDER_TO_FRONT.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyLockScreenFlags()
    }

    override fun onStart() {
        super.onStart()
        // If the phone is not locked this is a pre-launch call from the service
        // (done while MainActivity is in foreground). Hide immediately — the overlay
        // should only be visible on the lock screen.
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (!keyguard.isKeyguardLocked) {
            moveTaskToBack(false)
            return
        }
        registerReceiver(userPresentReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onResume() {
        super.onResume()
        // Re-apply on every resume so the flags are always current when the
        // activity comes to front, regardless of how it got there.
        applyLockScreenFlags()
        getSystemService(NotificationManager::class.java)
            .cancel(ScreenMonitorService.OVERLAY_NOTIFICATION_ID)
    }

    override fun onStop() {
        super.onStop()
        try { unregisterReceiver(userPresentReceiver) } catch (_: IllegalArgumentException) {}
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun applyLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        // Belt-and-suspenders: also set the window-level flags so the window
        // manager gets the hint even before ActivityManager propagates the change.
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        )
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    companion object {
        // Used by ScreenMonitorService to pre-launch the activity while the app is
        // in the foreground. onStart() auto-hides it if the phone is not locked.
        fun launch(context: Context) {
            context.startActivity(
                Intent(context, OverlayActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION,
                    )
                }
            )
        }
    }
}
