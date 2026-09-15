package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeSession: AcademicSessionEntity? = null,
    val allSessions: List<AcademicSessionEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<HomeUiState> = repository.getActiveSession()
        .flatMapLatest { session ->
            if (session == null) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                repository.getSubjectsForSession(session.id).flatMapLatest { subjects ->
                    repository.getAllSessions().flatMapLatest { sessions ->
                        flowOf(
                            HomeUiState(
                                activeSession = session,
                                allSessions = sessions,
                                subjects = subjects,
                                isLoading = false,
                            )
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true),
        )

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
}
