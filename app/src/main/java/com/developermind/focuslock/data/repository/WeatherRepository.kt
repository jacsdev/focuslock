package com.developermind.focuslock.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
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
    }

    fun observeTemperature(): Flow<TemperatureResult?> =
        appContext.weatherDataStore.data.map { prefs ->
            val temp = prefs[Keys.CACHED_TEMP] ?: return@map null
            val city = prefs[Keys.CACHE_CITY] ?: return@map null
            val timestamp = prefs[Keys.CACHE_TIMESTAMP] ?: return@map null
            TemperatureResult(temperature = temp, city = city, timestamp = timestamp)
        }

    suspend fun fetchAndCache(city: String): Result<Float> {
        val (lat, lon) = dataSource.geocode(city)
            ?: return Result.failure(Exception("City not found: $city"))
        val temp = dataSource.fetchTemperature(lat, lon)
            ?: return Result.failure(Exception("Failed to fetch temperature"))
        appContext.weatherDataStore.edit { prefs ->
            prefs[Keys.CACHED_TEMP] = temp
            prefs[Keys.CACHE_CITY] = city
            prefs[Keys.CACHE_TIMESTAMP] = System.currentTimeMillis()
        }
        return Result.success(temp)
    }

    suspend fun clearCache() {
        appContext.weatherDataStore.edit { it.clear() }
    }
}
