package com.aditya.present.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.data.SubjectEntity
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalHaptics
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = LocalHaptics.current
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
                AttendanceStatus.PRESENT -> "Present"
                AttendanceStatus.ABSENT -> "Absent"
                AttendanceStatus.CANCELLED -> "Cancelled"
                AttendanceStatus.HOLIDAY -> "Holiday"
                AttendanceStatus.ON_DUTY -> "On Duty"
            }
            snackbarHost.showSnackbar(
                message = "$name marked $statusText",
                actionLabel = "UNDO",
                withDismissAction = true,
            )
            lastMarked = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(title = { Text("Timetable", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            if (uiState.subjects.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptics.tap()
                        showAddSlot = true
                    },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add Slot") },
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
                            .semantics { contentDescription = "Select $dayName" },
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
                        title = "No subjects yet",
                        subtitle = "Add a subject first from the Home tab, then come back to create your timetable.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                uiState.slotsForDay.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Swipe,
                        title = "No classes on ${dayNames[selectedDay]}",
                        subtitle = "Tap \"Add Slot\" to schedule a class for this day. Swipe cards left/right to mark attendance.",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp),
                    ) {
                        items(uiState.slotsForDay, key = { it.slot.id }) { timetableSlot ->
                            timetableSlot.subject?.let { subject ->
                                SwipeableTimetableCard(
                                    subject = subject,
                                    startTimeMinutes = timetableSlot.slot.startTimeMinutes,
                                    units = timetableSlot.slot.units,
                                    todayStatus = timetableSlot.todayStatus,
                                    onMark = { status ->
                                        haptics.heavy()
                                        viewModel.markAttendance(subject.id, status, subject.name)
                                        lastMarked = subject.name to status
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

@Composable
private fun SwipeableTimetableCard(
    subject: SubjectEntity,
    startTimeMinutes: Int,
    units: Int,
    todayStatus: AttendanceStatus?,
    onMark: (AttendanceStatus) -> Unit,
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "offset")

    val hour = startTimeMinutes / 60
    val minute = startTimeMinutes % 60
    val endTimeMinutes = startTimeMinutes + units * 60
    val endHour = endTimeMinutes / 60
    val endMinute = endTimeMinutes % 60
    val timeText = String.format("%02d:%02d - %02d:%02d", hour, minute, endHour, endMinute)

    val bgColor = when {
        offsetX > 150f -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        offsetX < -150f -> Color(0xFFEF4444).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceContainer
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
                    contentDescription = "${subject.name} at $timeText. ${if (todayStatus != null) "Marked ${todayStatus.name.lowercase()}" else "Swipe right for present, left for absent"}."
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
                        text = String.format("until %02d:%02d", endHour, endMinute),
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
                    )
                    if (subject.teacherName.isNotBlank()) {
                        Text(
                            text = subject.teacherName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (units > 1) {
                        Text(
                            text = "$units units",
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
    val (color, label) = when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF4CAF50) to "Present"
        AttendanceStatus.ABSENT -> Color(0xFFEF4444) to "Absent"
        AttendanceStatus.CANCELLED -> Color(0xFFFF9800) to "Cancelled"
        AttendanceStatus.HOLIDAY -> Color(0xFF9E9E9E) to "Holiday"
        AttendanceStatus.ON_DUTY -> MaterialTheme.colorScheme.primary to "On Duty"
    }
    AssistChip(
        onClick = {},
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
    var selectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(9) }
    var selectedMinute by remember { mutableStateOf(0) }
    var units by remember { mutableStateOf(1) }

    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Class Slot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Day: ${dayNames[selectedDay]}", style = MaterialTheme.typography.bodyMedium)

                // Subject dropdown
                Box {
                    OutlinedTextField(
                        value = selectedSubject?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth().clickable { subjectMenuExpanded = true },
                        trailingIcon = {
                            TextButton(onClick = { subjectMenuExpanded = true }) { Text("Select") }
                        },
                    )
                    DropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false },
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name) },
                                onClick = {
                                    haptics.tap()
                                    selectedSubject = subject
                                    subjectMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                // Time picker button
                OutlinedTextField(
                    value = String.format("%02d:%02d", selectedHour, selectedMinute),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start time") },
                    modifier = Modifier.fillMaxWidth().clickable {
                        haptics.tap()
                        showTimePicker = true
                    },
                )

                // Units selector
                Text("Duration: $units hour${if (units > 1) "s" else ""}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3).forEach { u ->
                        AssistChip(
                            onClick = {
                                haptics.tap()
                                units = u
                            },
                            label = { Text("$u hr") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (units == u) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainer,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedSubject?.let { subject ->
                        val minutes = selectedHour * 60 + selectedMinute
                        onAdd(subject.id, minutes, units)
                    }
                },
                enabled = selectedSubject != null,
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
