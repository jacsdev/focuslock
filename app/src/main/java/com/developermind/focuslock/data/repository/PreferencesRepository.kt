package com.developermind.focuslock.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.developermind.focuslock.data.model.OverlayPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focuslock_settings")

class PreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_TEMPERATURE = booleanPreferencesKey("show_temperature")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
    }

    fun observePreferences(): Flow<OverlayPreferences> =
        appContext.dataStore.data.map { prefs ->
            OverlayPreferences(
                showBattery = prefs[Keys.SHOW_BATTERY] ?: true,
                showTemperature = prefs[Keys.SHOW_TEMPERATURE] ?: false,
                weatherCity = prefs[Keys.WEATHER_CITY] ?: "",
            )
        }

    suspend fun setShowBattery(value: Boolean) {
        appContext.dataStore.edit { it[Keys.SHOW_BATTERY] = value }
    }

    suspend fun setShowTemperature(value: Boolean) {
        appContext.dataStore.edit { it[Keys.SHOW_TEMPERATURE] = value }
    }

    suspend fun setWeatherCity(city: String) {
        appContext.dataStore.edit { it[Keys.WEATHER_CITY] = city }
    }
}
