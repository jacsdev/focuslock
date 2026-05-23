package com.developermind.focuslock.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.developermind.focuslock.data.model.AppTheme
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ThemeRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTheme(): AppTheme =
        AppTheme.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) }
            ?: AppTheme.DYNAMIC

    fun saveTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun observeTheme(): Flow<AppTheme> = callbackFlow {
        trySend(getTheme())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME) trySend(getTheme())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    companion object {
        private const val PREFS_NAME = "focuslock_prefs"
        private const val KEY_THEME = "theme"
    }
}
