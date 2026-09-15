package com.aditya.present.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalHaptics
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val haptics = LocalHaptics.current
    val uiState by viewModel.uiState.collectAsState()

    val displayedMonth by viewModel.monthOffset.collectAsState()
    val cal = remember(displayedMonth) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val monthName = remember(displayedMonth) {
        val fmt = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        fmt.format(cal.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            haptics.tap()
                            viewModel.setMonthOffset(displayedMonth - 1)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(R.string.calendar_previous_month),
                            )
                        }
                        Text(
                            text = monthName,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        IconButton(onClick = {
                            haptics.tap()
                            viewModel.setMonthOffset(displayedMonth + 1)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.calendar_next_month),
                            )
                        }
                    }
                },
                actions = {
                    if (displayedMonth != 0) {
                        TextButton(onClick = {
                            haptics.tap()
                            viewModel.setMonthOffset(0)
                        }) {
                            Text(stringResource(R.string.calendar_today))
                        }
                    }
                },
            )
        }
    ) { padding ->
        if (uiState.activeSession == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.calendar_no_session),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LegendDot(Color(0xFF4CAF50), stringResource(R.string.status_present))
                    LegendDot(Color(0xFFEF4444), stringResource(R.string.status_absent))
                    LegendDot(Color(0xFFFF9800), stringResource(R.string.status_cancelled))
                    LegendDot(MaterialTheme.colorScheme.primary, stringResource(R.string.status_on_duty))
                    LegendDot(Color(0xFF9C27B0), stringResource(R.string.status_holiday))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Day headers
                val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayNames.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
                val today = Calendar.getInstance()
                val isCurrentMonth = displayedMonth == 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        var dayCounter = 1
                        while (dayCounter <= daysInMonth) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                for (col in 0 until 7) {
                                    if (dayCounter > daysInMonth) {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    } else if (dayCounter == 1 && col < firstDayOfWeek) {
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    } else {
                                        val day = dayCounter
                                        val isToday = isCurrentMonth &&
                                            day == today.get(Calendar.DAY_OF_MONTH)
                                        val dayEntries = uiState.attendanceByDay[day] ?: emptyList()
                                        // Use the worst status for the day's background tint
                                        // Priority: ABSENT > CANCELLED > ON_DUTY > HOLIDAY > PRESENT
                                        val dayStatus = dayEntries.mapNotNull {
                                            runCatching { AttendanceStatus.valueOf(it.status) }.getOrNull()
                                        }.maxByOrNull {
                                            when (it) {
                                                AttendanceStatus.ABSENT -> 5
                                                AttendanceStatus.CANCELLED -> 4
                                                AttendanceStatus.ON_DUTY -> 3
                                                AttendanceStatus.HOLIDAY -> 2
                                                AttendanceStatus.PRESENT -> 1
                                            }
                                        }?.name

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(CircleShape)
                                                .background(
                                                    when (dayStatus) {
                                                        "PRESENT" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                                        "ABSENT" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                        "CANCELLED" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                                        "ON_DUTY" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                        "HOLIDAY" -> Color(0xFF9C27B0).copy(alpha = 0.15f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable {
                                                    haptics.tap()
                                                    viewModel.loadDayEntries(day)
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                            ) {
                                                Text(
                                                    text = "$day",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when (dayStatus) {
                                                        "PRESENT" -> Color(0xFF2E7D32)
                                                        "ABSENT" -> Color(0xFFC62828)
                                                        "CANCELLED" -> Color(0xFFE65100)
                                                        "ON_DUTY" -> MaterialTheme.colorScheme.primary
                                                        "HOLIDAY" -> Color(0xFF7B1FA2)
                                                        else -> if (isToday) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.onSurface
                                                    },
                                                )
                                                // Show small colored dots for each subject's status
                                                if (dayEntries.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    ) {
                                                        dayEntries.take(4).forEach { entry ->
                                                            val dotColor = when (entry.status) {
                                                                "PRESENT" -> Color(0xFF4CAF50)
                                                                "ABSENT" -> Color(0xFFEF4444)
                                                                "CANCELLED" -> Color(0xFFFF9800)
                                                                "ON_DUTY" -> MaterialTheme.colorScheme.primary
                                                                "HOLIDAY" -> Color(0xFF9C27B0)
                                                                else -> Color.Gray
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(dotColor),
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        dayCounter++
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            stringResource(R.string.calendar_month_summary),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val allEntries = uiState.attendanceByDay.values.flatten()
                        val present = allEntries.filter { it.status == "PRESENT" }.sumOf { it.units }
                        val absent = allEntries.filter { it.status == "ABSENT" }.sumOf { it.units }
                        val cancelled = allEntries.filter { it.status == "CANCELLED" }.sumOf { it.units }
                        val onDuty = allEntries.filter { it.status == "ON_DUTY" }.sumOf { it.units }
                        val holiday = allEntries.filter { it.status == "HOLIDAY" }.sumOf { it.units }
                        val total = present + absent
                        val pct = if (total > 0) (present.toFloat() * 100 / total).toInt() else 0

                        SummaryRow(stringResource(R.string.status_present), present, Color(0xFF4CAF50))
                        SummaryRow(stringResource(R.string.status_absent), absent, Color(0xFFEF4444))
                        SummaryRow(stringResource(R.string.status_cancelled), cancelled, Color(0xFFFF9800))
                        SummaryRow(stringResource(R.string.status_on_duty), onDuty, MaterialTheme.colorScheme.primary)
                        SummaryRow(stringResource(R.string.status_holiday), holiday, Color(0xFF9C27B0))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.calendar_attendance_pct, pct),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (pct >= (uiState.activeSession?.targetAttendancePercent?.toInt() ?: 75)) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    // Day detail bottom sheet
    val dayEntries by viewModel.selectedDayEntries.collectAsState()
    if (dayEntries.isNotEmpty()) {
        DayDetailSheet(
            entries = dayEntries,
            onUpdateStatus = { id, status ->
                haptics.confirm()
                viewModel.updateAttendanceStatus(id, status)
            },
            onDelete = { id ->
                haptics.heavy()
                viewModel.deleteAttendanceEntry(id)
            },
            onDismiss = { viewModel.clearDayEntries() },
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SummaryRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            "$count",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayDetailSheet(
    entries: List<DayEntry>,
    onUpdateStatus: (Long, AttendanceStatus) -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = LocalHaptics.current
    var deleteTarget by remember { mutableStateOf<Long?>(null) }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.calendar_delete_entry)) },
            text = { Text(stringResource(R.string.calendar_delete_entry_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(deleteTarget!!)
                    deleteTarget = null
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.calendar_attendance_entries),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            entries.forEach { entry ->
                val status = runCatching {
                    AttendanceStatus.valueOf(entry.attendance.status)
                }.getOrNull()
                val subjectName = entry.subject?.name ?: stringResource(R.string.calendar_unknown)
                val subjectColor = entry.subject?.color ?: 0xFF9E9E9E.toInt()
                val timeText = entry.slotStartTimeMinutes?.let {
                    String.format("%02d:%02d", it / 60, it % 60)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(subjectColor)),
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subjectName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (timeText != null) {
                                    Text(
                                        text = timeText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            IconButton(onClick = {
                                haptics.tap()
                                deleteTarget = entry.attendance.id
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.calendar_delete_entry_cd),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AttendanceStatus.entries.forEach { statusOption ->
                                val isSelected = status == statusOption
                                val statusLabelRes = when (statusOption) {
                                    AttendanceStatus.PRESENT -> R.string.status_present
                                    AttendanceStatus.ABSENT -> R.string.status_absent
                                    AttendanceStatus.CANCELLED -> R.string.status_cancelled
                                    AttendanceStatus.HOLIDAY -> R.string.status_holiday
                                    AttendanceStatus.ON_DUTY -> R.string.status_on_duty
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptics.tap()
                                        onUpdateStatus(entry.attendance.id, statusOption)
                                    },
                                    label = {
                                        Text(
                                            stringResource(statusLabelRes),
                                            fontSize = 11.sp,
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
