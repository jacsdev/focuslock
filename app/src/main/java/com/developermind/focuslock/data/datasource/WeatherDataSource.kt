package com.developermind.focuslock.data.datasource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder

data class CurrentWeather(
    val temperature: Float,
    val weatherCode: Int,
    val isDay: Boolean,
)

class WeatherDataSource {

    suspend fun geocode(city: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = URLEncoder.encode(city, "UTF-8")
            val json = URL("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1").readText()
            val results = JSONObject(json).getJSONArray("results")
            if (results.length() == 0) return@withContext null
            val first = results.getJSONObject(0)
            Pair(first.getDouble("latitude"), first.getDouble("longitude"))
        }.getOrNull()
    }

    suspend fun fetchCurrentWeather(lat: Double, lon: Double): CurrentWeather? = withContext(Dispatchers.IO) {
        runCatching {
            val json = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weather_code,is_day"
            ).readText()
            val current = JSONObject(json).getJSONObject("current")
            CurrentWeather(
                temperature = current.getDouble("temperature_2m").toFloat(),
                weatherCode = current.getInt("weather_code"),
                isDay = current.getInt("is_day") == 1,
            )
        }.getOrNull()
    }
}
