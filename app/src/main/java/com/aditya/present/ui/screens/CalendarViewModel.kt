package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.PresentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class CalendarUiState(
    val activeSession: AcademicSessionEntity? = null,
    val attendanceByDay: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    // Month offset: 0 = current, -1 = previous, +1 = next
    private val _monthOffset = MutableStateFlow(0)
    val monthOffset: StateFlow<Int> = _monthOffset.asStateFlow()

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
                            val byDay = entries.groupBy { entry ->
                                val entryCal = Calendar.getInstance().apply {
                                    timeInMillis = entry.date
                                }
                                entryCal.get(Calendar.DAY_OF_MONTH)
                            }.mapValues { (_, dayEntries) ->
                                val statuses = dayEntries.map { it.status }
                                when {
                                    statuses.contains("ABSENT") -> "ABSENT"
                                    statuses.contains("PRESENT") -> "PRESENT"
                                    statuses.contains("ON_DUTY") -> "ON_DUTY"
                                    statuses.contains("CANCELLED") -> "CANCELLED"
                                    statuses.contains("HOLIDAY") -> "HOLIDAY"
                                    else -> "PRESENT"
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
}
