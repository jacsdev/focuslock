package com.developermind.focuslock.ui.overlay

import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.model.BatteryState

data class OverlayUiState(
    val time: String = "",
    val date: String = "",
    val battery: BatteryState = BatteryState(),
    val theme: AppTheme = AppTheme.DYNAMIC,
)
