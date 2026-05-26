package com.developermind.focuslock.data.model

data class TemperatureResult(
    val temperature: Float,
    val city: String,
    val timestamp: Long,
    val weatherCode: Int? = null,
    val isDay: Boolean = true,
) {
    val isStale: Boolean
        get() = System.currentTimeMillis() - timestamp > 2 * 60 * 60 * 1000L

    val condition: WeatherCondition?
        get() = weatherCode?.let { WeatherCondition.from(it, isDay) }
}
