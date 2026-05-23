package com.developermind.focuslock.ui.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.developermind.focuslock.ui.theme.FocusLockTheme

class OverlayActivity : ComponentActivity() {

    private val viewModel: OverlayViewModel by viewModels()

    // Moves the task to background the moment the user unlocks.
    // Registered/unregistered with the activity's own visible lifecycle so it
    // can call moveTaskToBack() directly — no need to route through the service.
    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) moveTaskToBack(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        // Remove enter/exit animations so the overlay appears and disappears instantly.
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        hideSystemBars()

        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            FocusLockTheme {
                OverlayScreen(
                    uiState = uiState.value,
                    // Tap anywhere → move to background, revealing the lock screen.
                    onDismiss = { moveTaskToBack(false) },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(userPresentReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(userPresentReceiver)
        // Remove animation for the background transition as well.
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    companion object {
        // REORDER_TO_FRONT: if the activity is already alive in memory, Android brings it to the
        // foreground without recreating it — this is what eliminates the keyguard flash.
        // NO_ANIMATION: no enter transition, the overlay appears on the first rendered frame.
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
