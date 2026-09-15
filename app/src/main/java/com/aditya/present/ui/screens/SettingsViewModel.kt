package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.ThemePrefs
import com.aditya.present.data.ThemeRepository
import com.aditya.present.domain.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val themePrefs: StateFlow<ThemePrefs> = themeRepository.themePrefs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePrefs(),
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeRepository.setThemeMode(mode) }
    }

    fun setAccentName(name: String) {
        viewModelScope.launch { themeRepository.setAccentName(name) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setDynamicColor(enabled) }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setAnimationsEnabled(enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setHapticFeedback(enabled) }
    }

    fun setDefaultTargetPercent(percent: Int) {
        viewModelScope.launch { themeRepository.setDefaultTargetPercent(percent) }
    }

    fun toggleWeekendDay(day: String) {
        val current = themePrefs.value.weekendDays
        val days = current.split(",").filter { it.isNotBlank() }.toMutableList()
        if (days.contains(day)) days.remove(day) else days.add(day)
        viewModelScope.launch { themeRepository.setWeekendDays(days.joinToString(",")) }
    }

    fun setAutoMarkHour(hour: Int) {
        viewModelScope.launch { themeRepository.setAutoMarkHour(hour) }
    }

    fun setCardStyle(style: String) {
        viewModelScope.launch { themeRepository.setCardStyle(style) }
    }

    fun setShowPercentageOnCards(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setShowPercentageOnCards(enabled) }
    }

    fun setCompactMode(enabled: Boolean) {
        viewModelScope.launch { themeRepository.setCompactMode(enabled) }
    }
}
