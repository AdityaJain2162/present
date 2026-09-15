package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.AttendanceEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import com.aditya.present.data.SubjectWithStats
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.domain.StreakCalculator
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val overallAttended: Int = 0,
    val overallTotal: Int = 0,
    val monthlyAttended: Int = 0,
    val monthlyTotal: Int = 0,
    val weeklyAttended: Int = 0,
    val weeklyTotal: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val perfectDays: Int = 0,
    val trendData: List<Float> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    private val _lastMarked = MutableStateFlow<Pair<String, AttendanceStatus>?>(null)
    private val _lastMarkedId = MutableStateFlow<Long?>(null)
    private val markMutex = Mutex()

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
                            // Each flow returns a pair: (subject stats, raw entries for period computation)
                            val statsFlows = subjects.map { subject ->
                                repository.getAttendanceForSubject(subject.id).map { entries ->
                                    // Per AGENTS.md: totalUnits = PRESENT + ABSENT
                                    // (CANCELLED/HOLIDAY/ON_DUTY excluded)
                                    val attended = entries.count {
                                        it.status == AttendanceStatus.PRESENT.name
                                    }
                                    val total = entries.count {
                                        it.status == AttendanceStatus.PRESENT.name ||
                                            it.status == AttendanceStatus.ABSENT.name
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
                                    ) to entries
                                }
                            }
                            @Suppress("UNCHECKED_CAST")
                            combine(statsFlows) { statsArray ->
                                val pairs = statsArray as Array<Pair<SubjectWithAttendance, List<AttendanceEntity>>>
                                val subjectStats = pairs.map { it.first }
                                val allEntries = pairs.flatMap { it.second }

                                // Compute period-filtered overall stats
                                val now = System.currentTimeMillis()
                                val weekStart = getStartOfWeek()
                                val monthStart = getStartOfMonth()

                                val overallAttended = allEntries.count {
                                    it.status == AttendanceStatus.PRESENT.name
                                }
                                val overallTotal = allEntries.count {
                                    it.status == AttendanceStatus.PRESENT.name ||
                                        it.status == AttendanceStatus.ABSENT.name
                                }
                                val monthlyAttended = allEntries.filter { it.date >= monthStart }.count {
                                    it.status == AttendanceStatus.PRESENT.name
                                }
                                val monthlyTotal = allEntries.filter { it.date >= monthStart }.count {
                                    it.status == AttendanceStatus.PRESENT.name ||
                                        it.status == AttendanceStatus.ABSENT.name
                                }
                                val weeklyAttended = allEntries.filter { it.date >= weekStart }.count {
                                    it.status == AttendanceStatus.PRESENT.name
                                }
                                val weeklyTotal = allEntries.filter { it.date >= weekStart }.count {
                                    it.status == AttendanceStatus.PRESENT.name ||
                                        it.status == AttendanceStatus.ABSENT.name
                                }

                                val streakStats = StreakCalculator.calculate(allEntries)
                                val trendData = computeTrendData(allEntries)

                                HomeUiState(
                                    activeSession = session,
                                    allSessions = sessions,
                                    subjects = subjectStats,
                                    isLoading = false,
                                    lastMarkedSubject = _lastMarked.value?.first,
                                    lastMarkedStatus = _lastMarked.value?.second,
                                    overallAttended = overallAttended,
                                    overallTotal = overallTotal,
                                    monthlyAttended = monthlyAttended,
                                    monthlyTotal = monthlyTotal,
                                    weeklyAttended = weeklyAttended,
                                    weeklyTotal = weeklyTotal,
                                    currentStreak = streakStats.currentStreak,
                                    bestStreak = streakStats.bestStreak,
                                    perfectDays = streakStats.perfectDays,
                                    trendData = trendData,
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
            markMutex.withLock {
                val now = System.currentTimeMillis()
                // Check if today has timetable slots for this subject
                val todayDayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                val todaySlots = repository.getSlotsForSubjectOnDay(subjectId, todayDayOfWeek)

                if (todaySlots.isNotEmpty()) {
                    // Mark each slot for today
                    var lastId: Long? = null
                    for (slot in todaySlots) {
                        lastId = repository.upsertAttendance(
                            subjectId = subjectId,
                            date = now,
                            status = status,
                            slotId = slot.id,
                        )
                    }
                    _lastMarkedId.value = lastId
                } else {
                    // No slots today — mark a slotless entry
                    val id = repository.upsertAttendance(
                        subjectId = subjectId,
                        date = now,
                        status = status,
                    )
                    _lastMarkedId.value = id
                }
                _lastMarked.value = subjectName to status
            }
        }
    }

    fun undoLastMarked() {
        val id = _lastMarkedId.value ?: return
        viewModelScope.launch {
            markMutex.withLock {
                repository.deleteAttendanceById(id)
                _lastMarkedId.value = null
                _lastMarked.value = null
            }
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

    private fun getStartOfWeek(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun computeTrendData(entries: List<AttendanceEntity>): List<Float> {
        if (entries.isEmpty()) return emptyList()

        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val today = cal.timeInMillis

        val dayMs = 24 * 60 * 60 * 1000L
        val trend = mutableListOf<Float>()

        for (i in 13 downTo 0) {
            val dayStart = today - i * dayMs
            val dayEnd = dayStart + dayMs - 1
            val dayEntries = entries.filter { it.date in dayStart..dayEnd }
            val attended = dayEntries.count { it.status == AttendanceStatus.PRESENT.name }
            val total = dayEntries.count {
                it.status == AttendanceStatus.PRESENT.name || it.status == AttendanceStatus.ABSENT.name
            }
            trend.add(if (total > 0) attended.toFloat() / total else 0f)
        }
        return trend
    }
}
