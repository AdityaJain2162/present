package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.PresentRepository
import com.aditya.present.data.SubjectEntity
import com.aditya.present.util.AcronymGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditSubjectUiState(
    val name: String = "",
    val acronym: String = "",
    val color: Int = 0xFF006A6A.toInt(),
    val targetPercent: Float = 75f,
    val totalUnits: Int = 1,
    val teacherName: String = "",
    val isEdit: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class AddEditSubjectViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditSubjectUiState())
    val uiState: StateFlow<AddEditSubjectUiState> = _uiState.asStateFlow()

    fun loadSubjectForEdit(subject: SubjectEntity) {
        _uiState.update {
            it.copy(
                name = subject.name,
                acronym = subject.acronym,
                color = subject.color,
                targetPercent = subject.targetAttendancePercent,
                totalUnits = subject.totalUnits,
                teacherName = subject.teacherName,
                isEdit = true,
            )
        }
    }

    fun loadSubjectById(subjectId: Long) {
        viewModelScope.launch {
            val subject = repository.getSubjectById(subjectId) ?: return@launch
            loadSubjectForEdit(subject)
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(name = name, acronym = AcronymGenerator.generate(name))
        }
    }

    fun updateTeacherName(name: String) {
        _uiState.update { it.copy(teacherName = name) }
    }

    fun updateColor(color: Int) {
        _uiState.update { it.copy(color = color) }
    }

    fun updateTargetPercent(percent: Float) {
        _uiState.update { it.copy(targetPercent = percent) }
    }

    fun updateTotalUnits(units: Int) {
        _uiState.update { it.copy(totalUnits = units.coerceAtLeast(1)) }
    }

    fun save(sessionId: Long, existingId: Long? = null) {
        val state = _uiState.value
        if (state.name.isBlank()) return
        // In edit mode, wait for the subject to load before saving
        if (existingId != null && !state.isEdit) return

        viewModelScope.launch {
            if (existingId != null && state.isEdit) {
                repository.updateSubject(
                    SubjectEntity(
                        id = existingId,
                        sessionId = sessionId,
                        name = state.name,
                        acronym = state.acronym,
                        color = state.color,
                        targetAttendancePercent = state.targetPercent,
                        totalUnits = state.totalUnits,
                        teacherName = state.teacherName,
                    )
                )
            } else {
                repository.insertSubject(
                    sessionId = sessionId,
                    name = state.name,
                    acronym = state.acronym,
                    color = state.color,
                    target = state.targetPercent,
                    totalUnits = state.totalUnits,
                    teacherName = state.teacherName,
                )
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
