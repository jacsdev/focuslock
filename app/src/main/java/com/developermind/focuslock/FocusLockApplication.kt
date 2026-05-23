package com.developermind.focuslock

import android.app.Application
import com.developermind.focuslock.util.LocaleManager

class FocusLockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val savedTag = LocaleManager(this).getSavedLanguageTag()
        LocaleManager.applyLocale(savedTag)
    }
}
