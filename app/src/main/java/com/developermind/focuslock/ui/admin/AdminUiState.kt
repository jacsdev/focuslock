package com.developermind.focuslock.ui.admin

import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.model.WeatherCondition
import com.developermind.focuslock.util.LocaleManager

data class AdminUiState(
    val isFocusLockEnabled: Boolean = true,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.DYNAMIC,
    val selectedLanguageTag: String = LocaleManager.SYSTEM_DEFAULT,
    val showBattery: Boolean = true,
    val showTemperature: Boolean = false,
    val weatherCity: String = "",
    val cityEditState: CityEditState = CityEditState.Idle,
    val cachedTemperature: Float? = null,
    val temperatureIsStale: Boolean = false,
    val weatherCondition: WeatherCondition? = null,
)
