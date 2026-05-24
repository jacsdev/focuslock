package com.developermind.focuslock.ui.admin

import android.app.Application
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.developermind.focuslock.data.model.AppTheme
import com.developermind.focuslock.data.repository.PreferencesRepository
import com.developermind.focuslock.data.repository.ThemeRepository
import com.developermind.focuslock.data.repository.WeatherRepository
import com.developermind.focuslock.service.FocusLockAccessibilityService
import com.developermind.focuslock.util.LocaleManager
import com.developermind.focuslock.worker.WeatherSyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ThemeRepository(application)
    private val localeManager = LocaleManager(application)
    private val preferencesRepository = PreferencesRepository(application)
    private val weatherRepository = WeatherRepository(application)
    private val workManager = WorkManager.getInstance(application)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        themeRepository.observeTheme()
            .onEach { theme -> _uiState.update { it.copy(selectedTheme = theme) } }
            .launchIn(viewModelScope)

        preferencesRepository.observePreferences()
            .onEach { prefs ->
                _uiState.update {
                    it.copy(
                        showBattery = prefs.showBattery,
                        showTemperature = prefs.showTemperature,
                        weatherCity = prefs.weatherCity,
                    )
                }
            }
            .launchIn(viewModelScope)

        weatherRepository.observeTemperature()
            .onEach { result ->
                _uiState.update {
                    it.copy(
                        cachedTemperature = result?.temperature,
                        temperatureIsStale = result?.isStale ?: false,
                    )
                }
            }
            .launchIn(viewModelScope)

        _uiState.update { it.copy(selectedLanguageTag = localeManager.getSavedLanguageTag()) }

        // Reschedule weather sync on startup if city already exists
        viewModelScope.launch {
            val prefs = preferencesRepository.observePreferences().first()
            if (prefs.weatherCity.isNotBlank()) scheduleWeatherSync(prefs.weatherCity)
        }

        refreshPermissions()
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        val powerManager = context.getSystemService(PowerManager::class.java)
        _uiState.update {
            it.copy(
                isBatteryOptimizationIgnored = powerManager
                    .isIgnoringBatteryOptimizations(context.packageName),
                isAccessibilityServiceEnabled = FocusLockAccessibilityService.isEnabled(context),
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

    fun setShowBattery(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowBattery(value) }
    }

    fun setShowTemperature(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setShowTemperature(value) }
    }

    fun setWeatherCity(city: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cityEditState = CityEditState.Saving) }
            try {
                preferencesRepository.setWeatherCity(city)
                scheduleWeatherSync(city)
                _uiState.update { it.copy(cityEditState = CityEditState.Success) }
                delay(2_000)
                _uiState.update { it.copy(cityEditState = CityEditState.Idle) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cityEditState = CityEditState.Error) }
                delay(3_000)
                _uiState.update { it.copy(cityEditState = CityEditState.Idle) }
            }
        }
    }

    fun clearWeatherCity() {
        viewModelScope.launch {
            preferencesRepository.setWeatherCity("")
            weatherRepository.clearCache()
            workManager.cancelAllWorkByTag(WeatherSyncWorker.WORK_TAG)
        }
    }

    fun requestDeleteCity() {
        _uiState.update { it.copy(cityEditState = CityEditState.ConfirmDelete) }
    }

    fun cancelDeleteCity() {
        _uiState.update { it.copy(cityEditState = CityEditState.Idle) }
    }

    private fun scheduleWeatherSync(city: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = workDataOf(WeatherSyncWorker.KEY_CITY to city)

        workManager.enqueueUniqueWork(
            WeatherSyncWorker.WORK_ONCE_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<WeatherSyncWorker>()
                .setInputData(inputData)
                .setConstraints(constraints)
                .addTag(WeatherSyncWorker.WORK_TAG)
                .build(),
        )
        workManager.enqueueUniquePeriodicWork(
            WeatherSyncWorker.WORK_PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequestBuilder<WeatherSyncWorker>(30, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(constraints)
                .addTag(WeatherSyncWorker.WORK_TAG)
                .build(),
        )
    }
}
