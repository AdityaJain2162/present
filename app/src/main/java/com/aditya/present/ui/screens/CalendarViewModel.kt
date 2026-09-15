package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.PresentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import java.util.Calendar

data class CalendarUiState(
    val activeSession: AcademicSessionEntity? = null,
    // Map of day-of-month -> status string (PRESENT/ABSENT/CANCELLED/ON_DUTY)
    val attendanceByDay: Map<Int, String> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<CalendarUiState> = repository.getActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(CalendarUiState(isLoading = false))
            } else {
                // Get attendance for current month
                val cal = Calendar.getInstance()
                val startOfMonth = cal.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfMonth = cal.apply {
                    add(Calendar.MONTH, 1)
                    add(Calendar.MILLISECOND, -1)
                }.timeInMillis

                repository.getAttendanceForDate(startOfMonth, endOfMonth)
                    .map { entries ->
                        val byDay = entries.groupBy { entry ->
                            val entryCal = Calendar.getInstance().apply {
                                timeInMillis = entry.date
                            }
                            entryCal.get(Calendar.DAY_OF_MONTH)
                        }.mapValues { (_, entries) ->
                            // Priority: ABSENT > PRESENT > CANCELLED > ON_DUTY
                            val statuses = entries.map { it.status }
                            when {
                                statuses.contains("ABSENT") -> "ABSENT"
                                statuses.contains("PRESENT") -> "PRESENT"
                                statuses.contains("CANCELLED") -> "CANCELLED"
                                statuses.contains("ON_DUTY") -> "ON_DUTY"
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarUiState(isLoading = true),
        )
}
