package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.AttendanceEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import com.aditya.present.domain.AttendanceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DayEntry(
    val attendance: AttendanceEntity,
    val subject: SubjectEntity?,
)

data class CalendarDayStatus(
    val status: String,
    val subjectColor: Int,
)

data class CalendarUiState(
    val activeSession: AcademicSessionEntity? = null,
    val attendanceByDay: Map<Int, List<CalendarDayStatus>> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset.asStateFlow()

    private val _selectedDayEntries = MutableStateFlow<List<DayEntry>>(emptyList())
    val selectedDayEntries: StateFlow<List<DayEntry>> = _selectedDayEntries.asStateFlow()

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<CalendarUiState> = repository.getActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(CalendarUiState(isLoading = false))
            } else {
                _monthOffset.flatMapLatest { offset ->
                    val cal = Calendar.getInstance().apply {
                        add(Calendar.MONTH, offset)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val startOfMonth = cal.timeInMillis

                    val endCal = Calendar.getInstance().apply {
                        add(Calendar.MONTH, offset)
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        add(Calendar.MONTH, 1)
                        add(Calendar.MILLISECOND, -1)
                    }
                    val endOfMonth = endCal.timeInMillis

                    repository.getAttendanceForDate(startOfMonth, endOfMonth)
                        .map { entries ->
                            val subjects = repository.getAllSubjects().associateBy { it.id }
                            val byDay = entries.groupBy { entry ->
                                val entryCal = Calendar.getInstance().apply {
                                    timeInMillis = entry.date
                                }
                                entryCal.get(Calendar.DAY_OF_MONTH)
                            }.mapValues { (_, dayEntries) ->
                                dayEntries.map { entry ->
                                    CalendarDayStatus(
                                        status = entry.status,
                                        subjectColor = subjects[entry.subjectId]?.color ?: 0xFF9E9E9E.toInt(),
                                    )
                                }
                            }
                            CalendarUiState(
                                activeSession = session,
                                attendanceByDay = byDay,
                                isLoading = false,
                            )
                        }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarUiState(isLoading = true),
        )

    fun setMonthOffset(offset: Int) {
        _monthOffset.value = offset
    }

    fun loadDayEntries(day: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                add(Calendar.MONTH, _monthOffset.value)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis

            val entries = repository.getAttendanceForDateList(start, end)
            val subjects = repository.getAllSubjects().associateBy { it.id }
            _selectedDayEntries.value = entries.map { entry ->
                DayEntry(entry, subjects[entry.subjectId])
            }
        }
    }

    fun clearDayEntries() {
        _selectedDayEntries.value = emptyList()
    }

    fun updateAttendanceStatus(entryId: Long, newStatus: AttendanceStatus) {
        viewModelScope.launch {
            repository.updateAttendanceStatus(entryId, newStatus)
        }
    }

    fun deleteAttendanceEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteAttendanceById(entryId)
        }
    }
}
