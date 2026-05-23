package com.developermind.focuslock.ui.overlay

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.developermind.focuslock.data.repository.BatteryRepository
import com.developermind.focuslock.data.repository.ThemeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class OverlayViewModel(application: Application) : AndroidViewModel(application) {

    private val batteryRepository = BatteryRepository(application)
    private val themeRepository = ThemeRepository(application)

    private val _uiState = MutableStateFlow(OverlayUiState(time = currentTime(), date = currentDate()))
    val uiState: StateFlow<OverlayUiState> = _uiState.asStateFlow()

    init {
        tickTime()
        observeBattery()
        observeTheme()
    }

    private fun tickTime() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(time = currentTime(), date = currentDate()) }
                delay(1_000)
            }
        }
    }

    private fun observeBattery() {
        batteryRepository.observeBatteryState()
            .onEach { battery -> _uiState.update { it.copy(battery = battery) } }
            .launchIn(viewModelScope)
    }

    private fun observeTheme() {
        themeRepository.observeTheme()
            .onEach { theme -> _uiState.update { it.copy(theme = theme) } }
            .launchIn(viewModelScope)
    }

    private companion object {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun currentTime(): String = LocalTime.now().format(timeFormatter)

        fun currentDate(): String {
            val locale = Locale.getDefault()
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
            return LocalDate.now().format(formatter)
        }
    }
}
