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
}
