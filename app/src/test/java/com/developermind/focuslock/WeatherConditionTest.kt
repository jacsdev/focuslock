package com.developermind.focuslock

import com.developermind.focuslock.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun clear_respects_day_and_night() {
        assertEquals(WeatherCondition.CLEAR_DAY, WeatherCondition.from(0, isDay = true))
        assertEquals(WeatherCondition.CLEAR_NIGHT, WeatherCondition.from(0, isDay = false))
    }

    @Test
    fun partly_cloudy_respects_day_and_night() {
        assertEquals(WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.from(1, isDay = true))
        assertEquals(WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.from(2, isDay = true))
        assertEquals(WeatherCondition.PARTLY_CLOUDY_NIGHT, WeatherCondition.from(2, isDay = false))
    }

    @Test
    fun overcast_and_fog_map_to_cloudy() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.from(3, isDay = true))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.from(45, isDay = true))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.from(48, isDay = false))
    }

    @Test
    fun drizzle_rain_and_showers_map_to_rain() {
        listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82).forEach { code ->
            assertEquals("code $code", WeatherCondition.RAIN, WeatherCondition.from(code, isDay = true))
        }
    }

    @Test
    fun snow_codes_map_to_snow() {
        listOf(71, 73, 75, 77, 85, 86).forEach { code ->
            assertEquals("code $code", WeatherCondition.SNOW, WeatherCondition.from(code, isDay = true))
        }
    }

    @Test
    fun thunderstorm_codes_map_to_thunderstorm() {
        listOf(95, 96, 99).forEach { code ->
            assertEquals("code $code", WeatherCondition.THUNDERSTORM, WeatherCondition.from(code, isDay = true))
        }
    }

    @Test
    fun unknown_code_falls_back_to_cloudy() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.from(1234, isDay = true))
    }
}
