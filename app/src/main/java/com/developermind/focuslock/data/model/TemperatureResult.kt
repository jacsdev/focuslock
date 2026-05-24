package com.developermind.focuslock.data.model

data class TemperatureResult(
    val temperature: Float,
    val city: String,
    val timestamp: Long,
) {
    val isStale: Boolean
        get() = System.currentTimeMillis() - timestamp > 2 * 60 * 60 * 1000L
}
