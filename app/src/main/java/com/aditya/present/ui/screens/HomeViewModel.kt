package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.AttendanceEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import com.aditya.present.data.SubjectWithStats
import com.aditya.present.domain.AttendanceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectWithAttendance(
    val subject: SubjectEntity,
    val attendedUnits: Int,
    val totalUnits: Int,
    val percentage: Float,
    val todayStatus: AttendanceStatus? = null,
)

data class HomeUiState(
    val activeSession: AcademicSessionEntity? = null,
    val allSessions: List<AcademicSessionEntity> = emptyList(),
    val subjects: List<SubjectWithAttendance> = emptyList(),
    val isLoading: Boolean = true,
    val lastMarkedSubject: String? = null,
    val lastMarkedStatus: AttendanceStatus? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    private val _lastMarked = MutableStateFlow<Pair<String, AttendanceStatus>?>(null)

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<HomeUiState> = repository.getActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                repository.getSubjectsForSession(session.id).flatMapLatest { subjects ->
                    repository.getAllSessions().flatMapLatest { sessions ->
                        // For each subject, compute attendance stats
                        val startOfToday = getStartOfToday()
                        val endOfToday = getEndOfToday()

                        if (subjects.isEmpty()) {
                            flowOf(
                                HomeUiState(
                                    activeSession = session,
                                    allSessions = sessions,
                                    subjects = emptyList(),
                                    isLoading = false,
                                )
                            )
                        } else {
                            // Combine attendance flows for all subjects
                            val statsFlows = subjects.map { subject ->
                                repository.getAttendanceForSubject(subject.id).map { entries ->
                                    val attended = entries.count {
                                        it.status == AttendanceStatus.PRESENT.name ||
                                            it.status == AttendanceStatus.ON_DUTY.name
                                    }
                                    val total = entries.count {
                                        it.status != AttendanceStatus.CANCELLED.name
                                    }
                                    val pct = if (total > 0) attended.toFloat() / total else 0f
                                    val todayEntry = entries.find {
                                        it.date in startOfToday..endOfToday
                                    }
                                    SubjectWithAttendance(
                                        subject = subject,
                                        attendedUnits = attended,
                                        totalUnits = total,
                                        percentage = pct,
                                        todayStatus = todayEntry?.let {
                                            runCatching {
                                                AttendanceStatus.valueOf(it.status)
                                            }.getOrNull()
                                        },
                                    )
                                }
                            }
                            combine(statsFlows) { statsArray ->
                                HomeUiState(
                                    activeSession = session,
                                    allSessions = sessions,
                                    subjects = statsArray.toList(),
                                    isLoading = false,
                                    lastMarkedSubject = _lastMarked.value?.first,
                                    lastMarkedStatus = _lastMarked.value?.second,
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
            initialValue = HomeUiState(isLoading = true),
        )

    fun markAttendance(subjectId: Long, status: AttendanceStatus, subjectName: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertAttendance(
                subjectId = subjectId,
                date = now,
                status = status,
            )
            _lastMarked.value = subjectName to status
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun switchSession(sessionId: Long) {
        viewModelScope.launch {
            repository.setActiveSession(sessionId)
        }
    }

    fun clearLastMarked() {
        _lastMarked.value = null
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
