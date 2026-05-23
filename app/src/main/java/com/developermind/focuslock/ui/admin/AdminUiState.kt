package com.developermind.focuslock.ui.admin

import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.util.LocaleManager

data class AdminUiState(
    val isBatteryOptimizationIgnored: Boolean = false,
    val areNotificationsEnabled: Boolean = false,
    val canUseFullScreenIntent: Boolean = true,
    val selectedTheme: AppTheme = AppTheme.DYNAMIC,
    val selectedLanguageTag: String = LocaleManager.SYSTEM_DEFAULT,
)
