package com.aditya.present.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.data.SubjectEntity
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.primaryGradient
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = LocalHaptics.current
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1) }
    var showAddSlot by remember { mutableStateOf(false) }
    var lastMarked by remember { mutableStateOf<Pair<String, AttendanceStatus>?>(null) }

    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

    LaunchedEffect(selectedDay) {
        viewModel.setDay(selectedDay)
    }

    LaunchedEffect(lastMarked) {
        lastMarked?.let { (name, status) ->
            val statusText = when (status) {
                AttendanceStatus.PRESENT -> context.getString(R.string.status_present)
                AttendanceStatus.ABSENT -> context.getString(R.string.status_absent)
                AttendanceStatus.CANCELLED -> context.getString(R.string.status_cancelled)
                AttendanceStatus.HOLIDAY -> context.getString(R.string.status_holiday)
                AttendanceStatus.ON_DUTY -> context.getString(R.string.status_on_duty)
            }
            val result = snackbarHost.showSnackbar(
                message = context.getString(R.string.home_marked_status, name, statusText),
                actionLabel = context.getString(R.string.home_undo),
                withDismissAction = true,
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoLastMarked()
            }
            lastMarked = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timetable_title), fontWeight = FontWeight.Bold) },
                actions = {
                    if (selectedDay != today) {
                        TextButton(onClick = {
                            haptics.tap()
                            selectedDay = today
                        }) {
                            Text(stringResource(R.string.timetable_today))
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (uiState.subjects.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptics.tap()
                        showAddSlot = true
                    },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.timetable_add_slot)) },
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Week strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                dayNames.forEachIndexed { index, dayName ->
                    val isSelected = selectedDay == index
                    val isToday = today == index
                    val dayColor = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainer

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(dayColor)
                            .clickable {
                                haptics.tap()
                                selectedDay = index
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                            .semantics { contentDescription = context.getString(R.string.timetable_select_day, dayName) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (isToday) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
                                    ),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.subjects.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Add,
                        title = stringResource(R.string.timetable_no_subjects),
                        subtitle = stringResource(R.string.timetable_no_subjects_desc),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                uiState.slotsForDay.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Swipe,
                        title = stringResource(R.string.timetable_no_classes, dayNames[selectedDay]),
                        subtitle = stringResource(R.string.timetable_no_classes_desc),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
                    ) {
                        // Swipe hint
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Swipe,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.timetable_swipe_to_mark),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        items(uiState.slotsForDay, key = { it.slot.id }) { timetableSlot ->
                            timetableSlot.subject?.let { subject ->
                                SwipeableTimetableCard(
                                    subject = subject,
                                    slotId = timetableSlot.slot.id,
                                    startTimeMinutes = timetableSlot.slot.startTimeMinutes,
                                    units = timetableSlot.slot.units,
                                    todayStatus = timetableSlot.todayStatus,
                                    onMark = { status ->
                                        haptics.heavy()
                                        viewModel.markAttendance(subject.id, timetableSlot.slot.id, status)
                                        lastMarked = subject.name to status
                                    },
                                    onDeleteSlot = {
                                        haptics.heavy()
                                        viewModel.deleteSlot(timetableSlot.slot.id)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSlot) {
        AddSlotDialog(
            subjects = uiState.subjects,
            selectedDay = selectedDay,
            onDismiss = { showAddSlot = false },
            onAdd = { subjectId, startTimeMinutes, units ->
                viewModel.addSlot(subjectId, selectedDay + 1, startTimeMinutes, units)
                showAddSlot = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun SwipeableTimetableCard(
    subject: SubjectEntity,
    slotId: Long,
    startTimeMinutes: Int,
    units: Int,
    todayStatus: AttendanceStatus?,
    onMark: (AttendanceStatus) -> Unit,
    onDeleteSlot: () -> Unit,
) {
    val context = LocalContext.current
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "offset")
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val hour = startTimeMinutes / 60
    val minute = startTimeMinutes % 60
    val endTimeMinutes = startTimeMinutes + units * 60
    val endHour = (endTimeMinutes / 60) % 24
    val endMinute = endTimeMinutes % 60
    val timeText = String.format("%02d:%02d - %02d:%02d", hour, minute, endHour, endMinute)

    val bgColor = when {
        offsetX > 150f -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        offsetX < -150f -> Color(0xFFEF4444).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.timetable_delete_slot)) },
            text = { Text(stringResource(R.string.timetable_delete_slot_confirm, subject.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteSlot()
                    showDeleteConfirm = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(bgColor),
    ) {
        // Background icons revealed on swipe
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = if (offsetX > 0) Arrangement.Start else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (offsetX > 50f) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(28.dp),
                )
            }
            if (offsetX < -50f) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // Foreground card with swipe gesture
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffset.dp)
                .clip(CardShape)
                .combinedClickable(
                    onLongClick = { showDeleteConfirm = true },
                    onClick = {},
                )
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > 150f -> {
                                    onMark(AttendanceStatus.PRESENT)
                                    offsetX = 0f
                                }
                                offsetX < -150f -> {
                                    onMark(AttendanceStatus.ABSENT)
                                    offsetX = 0f
                                }
                                else -> offsetX = 0f
                            }
                        },
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-300f, 300f)
                    }
                }
                .semantics {
                    contentDescription = if (todayStatus != null) {
                        context.getString(R.string.timetable_marked_status, subject.name, timeText, todayStatus.name.lowercase())
                    } else {
                        context.getString(R.string.timetable_swipe_hint, subject.name, timeText)
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Time on left
                Column(modifier = Modifier.width(80.dp)) {
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.timetable_until, String.format("%02d:%02d", endHour, endMinute)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Color dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(subject.color)),
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Subject info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subject.teacherName.isNotBlank()) {
                        Text(
                            text = subject.teacherName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (units > 1) {
                        Text(
                            text = stringResource(R.string.timetable_units, units),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Status badge
                if (todayStatus != null) {
                    StatusPill(status = todayStatus)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: AttendanceStatus) {
    val (color, labelRes) = when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF4CAF50) to R.string.status_present
        AttendanceStatus.ABSENT -> Color(0xFFEF4444) to R.string.status_absent
        AttendanceStatus.CANCELLED -> Color(0xFFFF9800) to R.string.status_cancelled
        AttendanceStatus.HOLIDAY -> Color(0xFF9E9E9E) to R.string.status_holiday
        AttendanceStatus.ON_DUTY -> MaterialTheme.colorScheme.primary to R.string.status_on_duty
    }
    AssistChip(
        onClick = {},
        label = { Text(stringResource(labelRes), fontSize = 11.sp, fontWeight = FontWeight.Medium) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.1f),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSlotDialog(
    subjects: List<SubjectEntity>,
    selectedDay: Int,
    onDismiss: () -> Unit,
    onAdd: (subjectId: Long, startTimeMinutes: Int, units: Int) -> Unit,
) {
    val haptics = LocalHaptics.current
    val accentPreset = LocalAccentPreset.current
    var selectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var units by remember { mutableStateOf(1) }
    var customDuration by remember { mutableStateOf("") }
    var useCustomDuration by remember { mutableStateOf(false) }

    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    val effectiveUnits = if (useCustomDuration && customDuration.isNotBlank()) {
        customDuration.toIntOrNull()?.coerceAtLeast(1) ?: 1
    } else {
        units
    }

    val endHour = (selectedHour + effectiveUnits) % 24
    val endTimeText = String.format("%02d:%02d", endHour, selectedMinute)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryGradient(accentPreset)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(stringResource(R.string.timetable_add_class_slot), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.timetable_day_label, dayNames[selectedDay]),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Subject dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = {
                        haptics.tap()
                        subjectMenuExpanded = it
                    },
                ) {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.timetable_subject)) },
                        placeholder = { Text(stringResource(R.string.timetable_select_subject)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                    )
                    DropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false },
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(subject.color)),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = subject.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                },
                                onClick = {
                                    haptics.tap()
                                    selectedSubject = subject
                                    subjectMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                // Time picker button with end time preview
                OutlinedButton(
                    onClick = {
                        haptics.tap()
                        showTimePicker = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.timetable_start_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = String.format("%02d:%02d", selectedHour, selectedMinute),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.timetable_end_time, endTimeText),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Duration selector
                Text(
                    if (useCustomDuration) stringResource(R.string.timetable_custom_duration)
                    else stringResource(R.string.timetable_duration, units),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3).forEach { u ->
                        FilterChip(
                            selected = !useCustomDuration && units == u,
                            onClick = {
                                haptics.tap()
                                useCustomDuration = false
                                units = u
                            },
                            label = { Text(stringResource(R.string.timetable_hr, u)) },
                        )
                    }
                    FilterChip(
                        selected = useCustomDuration,
                        onClick = {
                            haptics.tap()
                            useCustomDuration = true
                        },
                        label = { Text(stringResource(R.string.timetable_custom)) },
                    )
                }
                if (useCustomDuration) {
                    OutlinedTextField(
                        value = customDuration,
                        onValueChange = { customDuration = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.timetable_custom_hours)) },
                        placeholder = { Text(stringResource(R.string.timetable_custom_hours_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedSubject?.let { subject ->
                        val minutes = selectedHour * 60 + selectedMinute
                        onAdd(subject.id, minutes, effectiveUnits)
                    }
                },
                enabled = selectedSubject != null && (!useCustomDuration || customDuration.isNotBlank()),
            ) { Text(stringResource(R.string.timetable_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timeState.hour
                    selectedMinute = timeState.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = { TimePicker(state = timeState) },
        )
    }
}
