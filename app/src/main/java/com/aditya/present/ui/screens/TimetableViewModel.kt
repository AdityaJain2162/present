package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.ClassSlotEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import com.aditya.present.domain.AttendanceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimetableSlot(
    val slot: ClassSlotEntity,
    val subject: SubjectEntity?,
    val todayStatus: AttendanceStatus? = null,
)

data class TimetableUiState(
    val activeSession: AcademicSessionEntity? = null,
    val subjects: List<SubjectEntity> = emptyList(),
    val slotsForDay: List<TimetableSlot> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    private var selectedDayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<TimetableUiState> = repository.getActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(TimetableUiState(isLoading = false))
            } else {
                repository.getSubjectsForSession(session.id).flatMapLatest { subjects ->
                    repository.getSlotsForDay(selectedDayOfWeek + 1).flatMapLatest { slots ->
                        val startOfToday = getStartOfToday()
                        val endOfToday = getEndOfToday()

                        if (slots.isEmpty()) {
                            flowOf(
                                TimetableUiState(
                                    activeSession = session,
                                    subjects = subjects,
                                    slotsForDay = emptyList(),
                                    isLoading = false,
                                )
                            )
                        } else {
                            // Get attendance for today for each subject
                            val slotFlows = slots.map { slot ->
                                val subject = subjects.find { it.id == slot.subjectId }
                                if (subject == null) {
                                    flowOf(TimetableSlot(slot, null, null))
                                } else {
                                    repository.getAttendanceForSubject(subject.id).map { entries ->
                                        val todayEntry = entries.find {
                                            it.date in startOfToday..endOfToday
                                        }
                                        TimetableSlot(
                                            slot = slot,
                                            subject = subject,
                                            todayStatus = todayEntry?.let {
                                                runCatching {
                                                    AttendanceStatus.valueOf(it.status)
                                                }.getOrNull()
                                            },
                                        )
                                    }
                                }
                            }
                            kotlinx.coroutines.flow.combine(slotFlows) { array ->
                                TimetableUiState(
                                    activeSession = session,
                                    subjects = subjects,
                                    slotsForDay = array.toList().sortedBy { it.slot.startTimeMinutes },
                                    isLoading = false,
                                )
                            }
                        }
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimetableUiState(isLoading = true),
        )

    fun setDay(dayOfWeek: Int) {
        selectedDayOfWeek = dayOfWeek
        // Trigger refresh by re-collecting
    }

    fun addSlot(subjectId: Long, dayOfWeek: Int, startTimeMinutes: Int, units: Int) {
        viewModelScope.launch {
            repository.insertSlot(subjectId, dayOfWeek, startTimeMinutes, units)
        }
    }

    fun markAttendance(subjectId: Long, status: AttendanceStatus, subjectName: String) {
        viewModelScope.launch {
            repository.insertAttendance(subjectId, System.currentTimeMillis(), status)
        }
    }

    private fun getStartOfToday(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfToday(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
