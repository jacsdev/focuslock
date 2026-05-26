package com.developermind.focuslock.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.developermind.focuslock.data.datasource.WeatherDataSource
import com.developermind.focuslock.data.model.TemperatureResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.weatherDataStore by preferencesDataStore(name = "weather_cache")

class WeatherRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataSource = WeatherDataSource()

    private object Keys {
        val CACHED_TEMP = floatPreferencesKey("cached_temp")
        val CACHE_CITY = stringPreferencesKey("cache_city")
        val CACHE_TIMESTAMP = longPreferencesKey("cache_timestamp")
        val CACHED_WEATHER_CODE = intPreferencesKey("cached_weather_code")
        val CACHED_IS_DAY = booleanPreferencesKey("cached_is_day")
    }

    fun observeTemperature(): Flow<TemperatureResult?> =
        appContext.weatherDataStore.data.map { prefs ->
            val temp = prefs[Keys.CACHED_TEMP] ?: return@map null
            val city = prefs[Keys.CACHE_CITY] ?: return@map null
            val timestamp = prefs[Keys.CACHE_TIMESTAMP] ?: return@map null
            TemperatureResult(
                temperature = temp,
                city = city,
                timestamp = timestamp,
                weatherCode = prefs[Keys.CACHED_WEATHER_CODE],
                isDay = prefs[Keys.CACHED_IS_DAY] ?: true,
            )
        }

    suspend fun fetchAndCache(city: String): Result<Float> {
        val (lat, lon) = dataSource.geocode(city)
            ?: return Result.failure(Exception("City not found: $city"))
        val weather = dataSource.fetchCurrentWeather(lat, lon)
            ?: return Result.failure(Exception("Failed to fetch weather"))
        appContext.weatherDataStore.edit { prefs ->
            prefs[Keys.CACHED_TEMP] = weather.temperature
            prefs[Keys.CACHE_CITY] = city
            prefs[Keys.CACHE_TIMESTAMP] = System.currentTimeMillis()
            prefs[Keys.CACHED_WEATHER_CODE] = weather.weatherCode
            prefs[Keys.CACHED_IS_DAY] = weather.isDay
        }
        return Result.success(weather.temperature)
    }

    suspend fun clearCache() {
        appContext.weatherDataStore.edit { it.clear() }
    }
}
