package com.developermind.focuslock.data.model

data class OverlayPreferences(
    val isEnabled: Boolean = true,
    val showBattery: Boolean = true,
    val showTemperature: Boolean = false,
    val weatherCity: String = "",
)
