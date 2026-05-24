package com.developermind.focuslock.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.developermind.focuslock.data.repository.BatteryRepository
import com.developermind.focuslock.data.repository.PreferencesRepository
import com.developermind.focuslock.data.repository.ThemeRepository
import com.developermind.focuslock.data.repository.WeatherRepository
import com.developermind.focuslock.receiver.ScreenReceiver
import com.developermind.focuslock.ui.overlay.OverlayScreen
import com.developermind.focuslock.ui.overlay.OverlayUiState
import com.developermind.focuslock.ui.theme.FocusLockTheme
import com.developermind.focuslock.util.ServiceLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class FocusLockAccessibilityService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val lifecycleOwner = ServiceLifecycleOwner()
    private var isOverlayAttached = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timeJob: Job? = null
    private val uiState = mutableStateOf(OverlayUiState())

    private val batteryRepository by lazy { BatteryRepository(this) }
    private val themeRepository by lazy { ThemeRepository(this) }
    private val preferencesRepository by lazy { PreferencesRepository(this) }
    private val weatherRepository by lazy { WeatherRepository(this) }
    private var screenReceiver: ScreenReceiver? = null

    override fun onServiceConnected() {
        windowManager = getSystemService(WindowManager::class.java)
        lifecycleOwner.onCreate()
        setupOverlayView()
        observeBattery()
        observeTheme()
        observePreferences()
        observeTemperature()
        registerScreenReceiver()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        safeRemoveOverlay()
        lifecycleOwner.onDestroy()
        serviceScope.cancel()
        screenReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) {
                Log.e(TAG, "Error al desregistrar ScreenReceiver: ${e.message}")
            }
        }
    }

    // ── Overlay ───────────────────────────────────────────────────────────────

    private fun setupOverlayView() {
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                FocusLockTheme {
                    OverlayScreen(
                        uiState = uiState.value,
                        onDismiss = ::safeRemoveOverlay,
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun safeAddOverlay() {
        if (isOverlayAttached) return
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (!keyguard.isKeyguardLocked) return

        // Use real display bounds so OEM window managers (MIUI, One UI) can't
        // shrink the overlay to the usable area by ignoring MATCH_PARENT.
        val bounds: Rect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds
        } else {
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(dm)
            Rect(0, 0, dm.widthPixels, dm.heightPixels)
        }

        val params = WindowManager.LayoutParams(
            bounds.width(),
            bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        try {
            windowManager.addView(overlayView, params)
            isOverlayAttached = true
            lifecycleOwner.onStart()
            lifecycleOwner.onResume()
            startTimeTicker()
        } catch (e: WindowManager.BadTokenException) {
            Log.e(TAG, "BadToken al añadir overlay: ${e.message}")
        } catch (e: IllegalArgumentException) {
            // View was already attached — sync the flag to reflect the real state.
            Log.e(TAG, "Vista ya adjunta al WindowManager: ${e.message}")
            isOverlayAttached = true
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en addView: ${e.message}")
        }
    }

    private fun safeRemoveOverlay() {
        if (!isOverlayAttached) return
        stopTimeTicker()
        try {
            lifecycleOwner.onPause()
            lifecycleOwner.onStop()
        } catch (e: Exception) {
            Log.e(TAG, "Error en lifecycle al ocultar overlay: ${e.message}")
        }
        try {
            windowManager.removeViewImmediate(overlayView)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Vista no registrada en WindowManager: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en removeView: ${e.message}")
        } finally {
            isOverlayAttached = false
        }
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private fun startTimeTicker() {
        timeJob?.cancel()
        timeJob = serviceScope.launch {
            while (true) {
                uiState.value = uiState.value.copy(
                    time = currentTime(),
                    date = currentDate(),
                )
                delay(1_000)
            }
        }
    }

    private fun stopTimeTicker() {
        timeJob?.cancel()
        timeJob = null
    }

    private fun observeBattery() {
        serviceScope.launch {
            batteryRepository.observeBatteryState().collect { battery ->
                uiState.value = uiState.value.copy(battery = battery)
            }
        }
    }

    private fun observeTheme() {
        serviceScope.launch {
            themeRepository.observeTheme().collect { theme ->
                uiState.value = uiState.value.copy(theme = theme)
            }
        }
    }

    private fun observePreferences() {
        serviceScope.launch {
            preferencesRepository.observePreferences().collect { prefs ->
                uiState.value = uiState.value.copy(
                    showBattery = prefs.showBattery,
                    showTemperature = prefs.showTemperature,
                    weatherCity = prefs.weatherCity,
                )
            }
        }
    }

    private fun observeTemperature() {
        serviceScope.launch {
            weatherRepository.observeTemperature().collect { result ->
                uiState.value = uiState.value.copy(
                    temperature = result?.temperature,
                    temperatureIsStale = result?.isStale ?: false,
                )
            }
        }
    }

    // ── Screen receiver ───────────────────────────────────────────────────────

    private fun registerScreenReceiver() {
        screenReceiver = ScreenReceiver(
            onScreenOn = { safeAddOverlay() },
            onScreenOff = {},
            onUserPresent = { safeRemoveOverlay() },
        )
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    companion object {
        private const val TAG = "FocusLockA11y"
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun currentTime(): String = LocalTime.now().format(timeFormatter)
        fun currentDate(): String = LocalDate.now()
            .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()))

        fun isEnabled(context: Context): Boolean {
            val expected = ComponentName(context, FocusLockAccessibilityService::class.java)
            val raw = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
            ) ?: return false
            return raw.split(":").any { ComponentName.unflattenFromString(it) == expected }
        }
    }
}
