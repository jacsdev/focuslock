package com.developermind.focuslock.data.model

data class BatteryState(
    val percentage: Int = 0,
    val isCharging: Boolean = false,
    val isPlugged: Boolean = false,
    val isFull: Boolean = false,
    val isLow: Boolean = false,
)
