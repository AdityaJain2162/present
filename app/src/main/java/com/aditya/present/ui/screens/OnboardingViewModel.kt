package com.aditya.present.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.present.data.PresentRepository
import com.aditya.present.domain.SessionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: PresentRepository,
) : ViewModel() {

    val hasActiveSession: StateFlow<Boolean?> = repository.getActiveSession()
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null, // null = loading, true = has session, false = needs onboarding
        )

    fun createSession(
        type: SessionType,
        name: String,
        startDate: Long,
        endDate: Long,
        target: Float,
        onCreated: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val id = repository.insertSession(name, type, startDate, endDate, target)
            onCreated(id)
        }
    }
}
