package com.developermind.focuslock.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.developermind.focuslock.R

data class LanguageOption(val tag: String, @StringRes val nameRes: Int)

class LocaleManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedLanguageTag(): String = prefs.getString(KEY_LANGUAGE, SYSTEM_DEFAULT) ?: SYSTEM_DEFAULT

    fun saveAndApply(languageTag: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageTag).apply()
        applyLocale(languageTag)
    }

    companion object {
        const val SYSTEM_DEFAULT = "system"
        private const val PREFS_NAME = "focuslock_prefs"
        private const val KEY_LANGUAGE = "language"

        val supportedLanguages = listOf(
            LanguageOption(SYSTEM_DEFAULT, R.string.lang_system_default),
            LanguageOption("en", R.string.lang_english),
            LanguageOption("es", R.string.lang_spanish),
            LanguageOption("pt-BR", R.string.lang_portuguese_br),
            LanguageOption("hi", R.string.lang_hindi),
        )

        fun applyLocale(languageTag: String) {
            val localeList = if (languageTag == SYSTEM_DEFAULT) LocaleListCompat.getEmptyLocaleList()
                            else LocaleListCompat.forLanguageTags(languageTag)
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        fun getCurrentTag(): String {
            val locales = AppCompatDelegate.getApplicationLocales()
            return if (locales.isEmpty) SYSTEM_DEFAULT else locales.toLanguageTags()
        }
    }
}
