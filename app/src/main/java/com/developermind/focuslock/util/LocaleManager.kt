package com.developermind.focuslock.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

data class LanguageOption(
    val tag: String,
    val englishName: String,
    val nativeName: String?,
)

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
            LanguageOption(SYSTEM_DEFAULT, "System default", null),
            LanguageOption("en",    "English",    null),
            LanguageOption("es",    "Spanish",    "Español"),
            LanguageOption("pt-BR", "Portuguese", "Português (Brasil)"),
            LanguageOption("hi",    "Hindi",      "हिन्दी"),
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
