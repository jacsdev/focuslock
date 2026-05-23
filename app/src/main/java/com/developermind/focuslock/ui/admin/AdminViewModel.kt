package com.developermind.focuslock.ui.admin

import android.app.Application
import android.app.NotificationManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.repository.ThemeRepository
import com.developermind.focuslock.util.LocaleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ThemeRepository(application)
    private val localeManager = LocaleManager(application)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        themeRepository.observeTheme()
            .onEach { theme -> _uiState.update { it.copy(selectedTheme = theme) } }
            .launchIn(viewModelScope)

        _uiState.update { it.copy(selectedLanguageTag = localeManager.getSavedLanguageTag()) }

        refreshPermissions()
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        val powerManager = context.getSystemService(PowerManager::class.java)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        _uiState.update {
            it.copy(
                isBatteryOptimizationIgnored = powerManager
                    .isIgnoringBatteryOptimizations(context.packageName),
                areNotificationsEnabled = NotificationManagerCompat
                    .from(context)
                    .areNotificationsEnabled(),
                canUseFullScreenIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    notificationManager.canUseFullScreenIntent()
                else
                    true,
            )
        }
    }

    fun setTheme(theme: AppTheme) {
        themeRepository.saveTheme(theme)
    }

    fun setLanguage(tag: String) {
        localeManager.saveAndApply(tag)
        _uiState.update { it.copy(selectedLanguageTag = tag) }
    }
}
