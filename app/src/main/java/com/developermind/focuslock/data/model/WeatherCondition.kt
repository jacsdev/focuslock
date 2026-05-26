package com.developermind.focuslock.data.model

enum class WeatherCondition {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    RAIN,
    SNOW,
    THUNDERSTORM;

    companion object {
        // Maps WMO weather interpretation codes (Open-Meteo `weather_code`) to the
        // 6-condition simple set. Day/night only differentiates clear and partly cloudy.
        fun from(code: Int, isDay: Boolean): WeatherCondition = when (code) {
            0 -> if (isDay) CLEAR_DAY else CLEAR_NIGHT
            1, 2 -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            3, 45, 48 -> CLOUDY
            in 51..67, in 80..82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> CLOUDY
        }
    }
}
