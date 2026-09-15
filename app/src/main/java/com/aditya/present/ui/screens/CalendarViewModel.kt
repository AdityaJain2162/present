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
import kotlinx.coroutines.flow.first
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
    val slotStartTimeMinutes: Int? = null,
)

data class CalendarDayStatus(
    val status: String,
    val subjectColor: Int,
    val units: Int = 1,
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
                            // Only show subjects from the active session
                            val subjects = repository.getSubjectsForSession(session.id).first()
                                .associateBy { it.id }
                            val sessionSubjectIds = subjects.keys
                            val filteredEntries = entries.filter { it.subjectId in sessionSubjectIds }
                            val byDay = filteredEntries.groupBy { entry ->
                                val entryCal = Calendar.getInstance().apply {
                                    timeInMillis = entry.date
                                }
                                entryCal.get(Calendar.DAY_OF_MONTH)
                            }.mapValues { (_, dayEntries) ->
                                dayEntries.map { entry ->
                                    CalendarDayStatus(
                                        status = entry.status,
                                        subjectColor = subjects[entry.subjectId]?.color ?: 0xFF9E9E9E.toInt(),
                                        units = entry.units,
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
        clearDayEntries()
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
            val session = repository.getActiveSession().first()
            val subjects = if (session != null) {
                repository.getSubjectsForSession(session.id).first().associateBy { it.id }
            } else {
                emptyMap()
            }
            val sessionSubjectIds = subjects.keys
            // Load all slots to find slot times for entries
            val allSlots = repository.getAllSlots().associateBy { it.id }
            _selectedDayEntries.value = entries
                .filter { it.subjectId in sessionSubjectIds }
                .map { entry ->
                    DayEntry(
                        attendance = entry,
                        subject = subjects[entry.subjectId],
                        slotStartTimeMinutes = entry.slotId?.let { allSlots[it]?.startTimeMinutes },
                    )
                }
            _loadedDay = day
        }
    }

    fun clearDayEntries() {
        _selectedDayEntries.value = emptyList()
        _loadedDay = null
    }

    private var _loadedDay: Int? = null

    fun updateAttendanceStatus(entryId: Long, newStatus: AttendanceStatus) {
        viewModelScope.launch {
            repository.updateAttendanceStatus(entryId, newStatus)
            // Reload the day entries to reflect the change
            _loadedDay?.let { loadDayEntries(it) }
        }
    }

    fun deleteAttendanceEntry(entryId: Long) {
        viewModelScope.launch {
            repository.deleteAttendanceById(entryId)
            // Reload the day entries to reflect the deletion
            _loadedDay?.let { loadDayEntries(it) }
        }
    }
}
