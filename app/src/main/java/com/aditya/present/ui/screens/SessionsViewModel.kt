package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.domain.SessionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionWithStats(
    val session: AcademicSessionEntity,
    val subjectCount: Int,
)

data class SessionsUiState(
    val sessions: List<SessionWithStats> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<SessionsUiState> = repository.getAllSessions()
        .flatMapLatest { sessions ->
            if (sessions.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(SessionsUiState(isLoading = false))
            } else {
                kotlinx.coroutines.flow.combine(
                    sessions.map { session ->
                        repository.getSubjectsForSession(session.id).map { subjects ->
                            SessionWithStats(session, subjects.size)
                        }
                    }
                ) { statsArray ->
                    SessionsUiState(
                        sessions = statsArray.toList(),
                        isLoading = false,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionsUiState(isLoading = true),
        )

    fun createSession(name: String, type: SessionType, startDate: Long, endDate: Long, target: Float) {
        viewModelScope.launch {
            repository.insertSession(name, type, startDate, endDate, target)
        }
    }

    fun updateSession(session: AcademicSessionEntity, name: String, type: SessionType, startDate: Long, endDate: Long, target: Float) {
        viewModelScope.launch {
            repository.updateSession(
                session.copy(
                    name = name,
                    type = type.name,
                    startDate = startDate,
                    endDate = endDate,
                    targetAttendancePercent = target,
                )
            )
        }
    }

    fun deleteSession(session: AcademicSessionEntity) {
        viewModelScope.launch {
            repository.deleteSession(session)
            // If the deleted session was active, activate the most recent remaining one
            if (session.isActive) {
                val remaining = repository.getAllSessionsList()
                if (remaining.isNotEmpty()) {
                    repository.setActiveSession(remaining.first().id)
                }
            }
        }
    }

    fun switchSession(sessionId: Long) {
        viewModelScope.launch {
            repository.setActiveSession(sessionId)
        }
    }
}
