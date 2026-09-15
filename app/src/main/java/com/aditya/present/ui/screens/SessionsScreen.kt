package com.aditya.present.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.data.AcademicSessionEntity
import com.aditya.present.domain.SessionType
import com.aditya.present.ui.components.BannerAd
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalHaptics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    onBack: () -> Unit,
    viewModel: SessionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = LocalHaptics.current
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sessions_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptics.tap()
                    showCreateDialog = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.sessions_add_new)) },
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.sessions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.sessions_no_sessions),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp, horizontal = 0.dp),
            ) {
                items(uiState.sessions, key = { it.session.id }) { sessionWithStats ->
                    var showEditDialog by remember { mutableStateOf(false) }
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    SessionCard(
                        sessionWithStats = sessionWithStats,
                        onSwitch = {
                            haptics.confirm()
                            viewModel.switchSession(sessionWithStats.session.id)
                        },
                        onEdit = {
                            haptics.tap()
                            showEditDialog = true
                        },
                        onDelete = {
                            haptics.heavy()
                            showDeleteConfirm = true
                        },
                    )

                    if (showEditDialog) {
                        EditSessionDialog(
                            session = sessionWithStats.session,
                            onDismiss = { showEditDialog = false },
                            onUpdate = { name, type, start, end, target ->
                                viewModel.updateSession(
                                    sessionWithStats.session,
                                    name, type, start, end, target,
                                )
                                showEditDialog = false
                            },
                        )
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text(stringResource(R.string.sessions_delete)) },
                            text = {
                                Text(
                                    stringResource(R.string.sessions_delete_confirm, sessionWithStats.session.name),
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteSession(sessionWithStats.session)
                                    showDeleteConfirm = false
                                }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
                            },
                        )
                    }
                }
            }
        }

            // AdMob banner pinned at the bottom — always visible
            BannerAd()
        }
    }

    if (showCreateDialog) {
        CreateSessionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, type, start, end, target ->
                viewModel.createSession(name, type, start, end, target)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun SessionCard(
    sessionWithStats: SessionWithStats,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateFmt = remember { SimpleDateFormat("MMM yyyy", Locale.getDefault()) }
    val session = sessionWithStats.session

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (session.isActive)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer,
        ),
        onClick = {
            if (!session.isActive) onSwitch()
        },
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (session.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = session.type.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (session.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.sessions_edit_cd),
                        tint = if (session.isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.sessions_delete_cd),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                if (session.isActive) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = stringResource(R.string.sessions_active),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${dateFmt.format(Date(session.startDate))} — ${dateFmt.format(Date(session.endDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (session.isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.sessions_subjects_count, sessionWithStats.subjectCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (session.isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSessionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, SessionType, Long, Long, Float) -> Unit,
) {
    val haptics = LocalHaptics.current
    var name by remember { mutableStateOf("") }
    var sessionType by remember { mutableStateOf(SessionType.SEMESTER) }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 180) }
    var targetAttendance by remember { mutableStateOf(75f) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val defaultSemesterName = stringResource(R.string.sessions_default_semester_name)
    val defaultYearName = stringResource(R.string.sessions_default_year_name)

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startDate = it }
                    showStartPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endDate = it }
                    showEndPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sessions_add_new)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.sessions_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sessionType == SessionType.SEMESTER,
                        onClick = {
                            haptics.tap()
                            sessionType = SessionType.SEMESTER
                        },
                        label = { Text(stringResource(R.string.sessions_type_semester)) },
                    )
                    FilterChip(
                        selected = sessionType == SessionType.YEARLY,
                        onClick = {
                            haptics.tap()
                            sessionType = SessionType.YEARLY
                        },
                        label = { Text(stringResource(R.string.sessions_type_yearly)) },
                    )
                }

                OutlinedTextField(
                    value = dateFmt.format(Date(startDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.onboarding_start_date, "")) },
                    modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true },
                )

                OutlinedTextField(
                    value = dateFmt.format(Date(endDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.onboarding_end_date, "")) },
                    modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true },
                )

                Text(
                    text = stringResource(R.string.onboarding_target_label, targetAttendance.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = targetAttendance,
                    onValueChange = { targetAttendance = it },
                    valueRange = 50f..100f,
                    steps = 9,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        name.ifBlank {
                            if (sessionType == SessionType.SEMESTER) defaultSemesterName else defaultYearName
                        },
                        sessionType,
                        startDate,
                        endDate,
                        targetAttendance,
                    )
                },
                enabled = endDate > startDate,
            ) { Text(stringResource(R.string.sessions_create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSessionDialog(
    session: AcademicSessionEntity,
    onDismiss: () -> Unit,
    onUpdate: (String, SessionType, Long, Long, Float) -> Unit,
) {
    val haptics = LocalHaptics.current
    var name by remember { mutableStateOf(session.name) }
    var sessionType by remember { mutableStateOf(runCatching { SessionType.valueOf(session.type) }.getOrDefault(SessionType.SEMESTER)) }
    var startDate by remember { mutableStateOf(session.startDate) }
    var endDate by remember { mutableStateOf(session.endDate) }
    var targetAttendance by remember { mutableStateOf(session.targetAttendancePercent) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val defaultSemesterName = stringResource(R.string.sessions_default_semester_name)
    val defaultYearName = stringResource(R.string.sessions_default_year_name)

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startDate = it }
                    showStartPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endDate = it }
                    showEndPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sessions_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.sessions_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sessionType == SessionType.SEMESTER,
                        onClick = {
                            haptics.tap()
                            sessionType = SessionType.SEMESTER
                        },
                        label = { Text(stringResource(R.string.sessions_type_semester)) },
                    )
                    FilterChip(
                        selected = sessionType == SessionType.YEARLY,
                        onClick = {
                            haptics.tap()
                            sessionType = SessionType.YEARLY
                        },
                        label = { Text(stringResource(R.string.sessions_type_yearly)) },
                    )
                }

                OutlinedTextField(
                    value = dateFmt.format(Date(startDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.onboarding_start_date, "")) },
                    modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true },
                )

                OutlinedTextField(
                    value = dateFmt.format(Date(endDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.onboarding_end_date, "")) },
                    modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true },
                )

                Text(
                    text = stringResource(R.string.onboarding_target_label, targetAttendance.toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Slider(
                    value = targetAttendance,
                    onValueChange = { targetAttendance = it },
                    valueRange = 50f..100f,
                    steps = 9,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(
                        name.ifBlank {
                            if (sessionType == SessionType.SEMESTER) defaultSemesterName else defaultYearName
                        },
                        sessionType,
                        startDate,
                        endDate,
                        targetAttendance,
                    )
                },
                enabled = endDate > startDate,
            ) { Text(stringResource(R.string.save_subject)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
