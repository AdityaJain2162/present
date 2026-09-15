package com.aditya.present.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.ThemePrefs
import com.aditya.present.data.ThemeRepository
import com.aditya.present.domain.ThemeMode
import com.aditya.present.util.ExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
    private val presentRepository: PresentRepository,
) : ViewModel() {

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

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

    fun setHapticIntensity(intensity: String) {
        viewModelScope.launch { themeRepository.setHapticIntensity(intensity) }
    }

    fun exportCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val success = ExportUtil.exportToCsv(context, presentRepository, uri)
            _exportMessage.value = if (success) "Export successful" else "Export failed"
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}
