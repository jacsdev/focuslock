package com.developermind.focuslock.ui.overlay

import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.model.BatteryState
import com.developermind.focuslock.data.model.WeatherCondition

data class OverlayUiState(
    val battery: BatteryState = BatteryState(),
    val theme: AppTheme = AppTheme.DYNAMIC,
    val showBattery: Boolean = true,
    val showTemperature: Boolean = false,
    val weatherCity: String = "",
    val temperature: Float? = null,
    val temperatureIsStale: Boolean = false,
    val weatherCondition: WeatherCondition? = null,
    val navigationBarInsetPx: Int = 0,
)
