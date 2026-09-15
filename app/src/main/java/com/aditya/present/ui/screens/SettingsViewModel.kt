package com.aditya.present.ui.screens

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.R
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.ThemePrefs
import com.aditya.present.data.ThemeRepository
import com.aditya.present.domain.ThemeMode
import com.aditya.present.util.AlarmScheduler
import com.aditya.present.util.ExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val appContext: Context,
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
        viewModelScope.launch {
            themeRepository.setAutoMarkHour(hour)
            // Reschedule the alarm if auto-mark is enabled
            if (themePrefs.value.autoMarkEnabled) {
                AlarmScheduler.scheduleAutoMark(appContext, hour)
            }
        }
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

    fun setClassNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setClassNotificationsEnabled(enabled)
            if (enabled) {
                // Schedule reminders for all existing slots
                val slots = presentRepository.getAllSlots()
                val today = java.util.Calendar.getInstance()
                for (slot in slots) {
                    val subject = presentRepository.getSubjectById(slot.subjectId) ?: continue
                    val todayDayOfWeek = today.get(java.util.Calendar.DAY_OF_WEEK)
                    var daysUntil = (slot.dayOfWeek - todayDayOfWeek + 7) % 7
                    if (daysUntil == 0) {
                        val classTime = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, slot.startTimeMinutes / 60)
                            set(java.util.Calendar.MINUTE, slot.startTimeMinutes % 60)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        if (classTime.timeInMillis <= System.currentTimeMillis()) {
                            daysUntil = 7
                        }
                    }
                    val nextDate = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, daysUntil)
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }
                    AlarmScheduler.scheduleClassReminder(
                        context = appContext,
                        slot = slot,
                        subject = subject,
                        dateMillis = nextDate.timeInMillis,
                        leadMinutes = themePrefs.value.notificationLeadMinutes,
                    )
                }
            } else {
                // Cancel all existing class reminder alarms
                val slots = presentRepository.getAllSlots()
                for (slot in slots) {
                    AlarmScheduler.cancelClassReminder(appContext, slot.id)
                }
            }
        }
    }

    fun setNotificationLeadMinutes(minutes: Int) {
        viewModelScope.launch { themeRepository.setNotificationLeadMinutes(minutes) }
    }

    fun setAutoMarkEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setAutoMarkEnabled(enabled)
            if (enabled) {
                val prefs = themePrefs.value
                AlarmScheduler.scheduleAutoMark(appContext, prefs.autoMarkHour)
            } else {
                AlarmScheduler.cancelAutoMark(appContext)
            }
        }
    }

    fun exportCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val success = ExportUtil.exportToCsv(context, presentRepository, uri)
            _exportMessage.value = if (success)
                appContext.getString(R.string.export_success, uri.toString())
            else
                appContext.getString(R.string.export_failed)
        }
    }

    fun importCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = ExportUtil.importFromCsv(context, presentRepository, uri)
            _exportMessage.value = result.fold(
                onSuccess = { appContext.getString(R.string.import_success) },
                onFailure = { appContext.getString(R.string.import_failed, it.message ?: "") },
            )
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}
