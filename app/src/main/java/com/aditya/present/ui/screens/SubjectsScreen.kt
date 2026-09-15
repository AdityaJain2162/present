package com.aditya.present.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.components.SubjectCard
import com.aditya.present.ui.theme.LocalAnimationsEnabled
import com.aditya.present.ui.theme.LocalHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    onAddSubject: (Long) -> Unit,
    onEditSubject: (Long, Long) -> Unit = { _, _ -> },
    onBunkCalculator: (Long, String, Int, Int, Int, Float) -> Unit = { _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val animations = LocalAnimationsEnabled.current
    val haptics = LocalHaptics.current
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    var markingSubject by remember { mutableStateOf<SubjectWithAttendance?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(SubjectFilter.ALL) }

    // Show snackbar when attendance is marked
    val lastMarked = uiState.lastMarkedSubject
    val lastStatus = uiState.lastMarkedStatus
    LaunchedEffect(lastMarked, lastStatus) {
        if (lastMarked != null && lastStatus != null) {
            val statusText = when (lastStatus) {
                com.aditya.present.domain.AttendanceStatus.PRESENT -> context.getString(R.string.status_present)
                com.aditya.present.domain.AttendanceStatus.ABSENT -> context.getString(R.string.status_absent)
                com.aditya.present.domain.AttendanceStatus.CANCELLED -> context.getString(R.string.status_cancelled)
                com.aditya.present.domain.AttendanceStatus.HOLIDAY -> context.getString(R.string.status_holiday)
                com.aditya.present.domain.AttendanceStatus.ON_DUTY -> context.getString(R.string.status_on_duty)
            }
            val result = snackbarHost.showSnackbar(
                message = context.getString(R.string.home_marked_status, lastMarked, statusText),
                actionLabel = context.getString(R.string.home_undo),
                withDismissAction = true,
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoLastMarked()
            }
            viewModel.clearLastMarked()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(title = { Text("Subjects") })
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.subjects.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.Add,
                    title = stringResource(R.string.home_welcome_title),
                    subtitle = stringResource(R.string.home_welcome_subtitle),
                    modifier = Modifier.fillMaxSize().padding(padding),
                    action = {
                        TextButton(
                            onClick = {
                                haptics.tap()
                                uiState.activeSession?.let { onAddSubject(it.id) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(stringResource(R.string.home_add_first_subject), fontWeight = FontWeight.Medium)
                        }
                    },
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Search bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search subjects...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // Filter chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(SubjectFilter.entries.toList()) { filter ->
                                FilterChip(
                                    selected = filterMode == filter,
                                    onClick = {
                                        haptics.tap()
                                        filterMode = filter
                                    },
                                    label = { Text(filter.label) },
                                )
                            }
                        }
                    }

                    // Subject list with staggered animation
                    itemsIndexed(
                        uiState.subjects.filter { subject ->
                            val matchesSearch = searchQuery.isBlank() ||
                                subject.subject.name.contains(searchQuery, ignoreCase = true) ||
                                subject.subject.acronym.contains(searchQuery, ignoreCase = true) ||
                                subject.subject.teacherName.contains(searchQuery, ignoreCase = true)
                            val matchesFilter = when (filterMode) {
                                SubjectFilter.ALL -> true
                                SubjectFilter.BELOW_TARGET -> subject.totalUnits > 0 &&
                                    subject.percentage < (subject.subject.targetAttendancePercent / 100f)
                                SubjectFilter.MARKED_TODAY -> subject.todayStatus != null
                                SubjectFilter.UNMARKED -> subject.todayStatus == null
                            }
                            matchesSearch && matchesFilter
                        },
                        key = { _, s -> s.subject.id },
                    ) { index, subjectWithAtt ->
                        val delayMs = if (animations) index * 40 else 0
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = androidx.compose.animation.fadeIn(
                                androidx.compose.animation.core.tween(300, delayMillis = delayMs),
                            ) + androidx.compose.animation.slideInVertically(
                                androidx.compose.animation.core.tween(300, delayMillis = delayMs),
                            ) { it / 4 },
                        ) {
                            SubjectCard(
                                subject = subjectWithAtt.subject,
                                attendedUnits = subjectWithAtt.attendedUnits,
                                totalUnits = subjectWithAtt.totalUnits,
                                percentage = subjectWithAtt.percentage,
                                todayStatus = subjectWithAtt.todayStatus,
                                onClick = {
                                    haptics.tap()
                                    markingSubject = subjectWithAtt
                                },
                                onBunkCalculator = {
                                    haptics.tap()
                                    onBunkCalculator(
                                        subjectWithAtt.subject.id,
                                        subjectWithAtt.subject.name,
                                        subjectWithAtt.subject.color,
                                        subjectWithAtt.attendedUnits,
                                        subjectWithAtt.totalUnits,
                                        subjectWithAtt.subject.targetAttendancePercent,
                                    )
                                },
                                onEdit = {
                                    haptics.tap()
                                    uiState.activeSession?.let { session ->
                                        onEditSubject(session.id, subjectWithAtt.subject.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    // Attendance marking bottom sheet
    markingSubject?.let { subject ->
        com.aditya.present.ui.components.AttendanceMarkSheet(
            subjectName = subject.subject.name,
            subjectColor = subject.subject.color,
            teacherName = subject.subject.teacherName,
            onMark = { status ->
                viewModel.markAttendance(subject.subject.id, status, subject.subject.name)
                markingSubject = null
            },
            onDismiss = { markingSubject = null },
        )
    }
}
