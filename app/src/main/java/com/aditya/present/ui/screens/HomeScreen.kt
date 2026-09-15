package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.components.AttendanceMarkSheet
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.components.SubjectCard
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalAnimationsEnabled
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.primaryGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubject: (Long) -> Unit,
    onSubjectClick: (Long) -> Unit,
    onManageSessions: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accentPreset = LocalAccentPreset.current
    val animations = LocalAnimationsEnabled.current
    val haptics = LocalHaptics.current
    val context = LocalContext.current
    val snackbarHost = remember { SnackbarHostState() }

    var markingSubject by remember { mutableStateOf<SubjectWithAttendance?>(null) }

    // Show snackbar when attendance is marked
    val lastMarked = uiState.lastMarkedSubject
    val lastStatus = uiState.lastMarkedStatus
    androidx.compose.runtime.LaunchedEffect(lastMarked, lastStatus) {
        if (lastMarked != null && lastStatus != null) {
            val statusText = when (lastStatus) {
                AttendanceStatus.PRESENT -> context.getString(R.string.status_present)
                AttendanceStatus.ABSENT -> context.getString(R.string.status_absent)
                AttendanceStatus.CANCELLED -> context.getString(R.string.status_cancelled)
                AttendanceStatus.HOLIDAY -> context.getString(R.string.status_holiday)
                AttendanceStatus.ON_DUTY -> context.getString(R.string.status_on_duty)
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
            TopAppBar(
                title = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    Box {
                        TextButton(onClick = { menuExpanded = true }) {
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.activeSession?.name ?: stringResource(R.string.home_no_session),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (uiState.allSessions.size > 1) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = stringResource(R.string.home_switch_session),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            uiState.allSessions.forEach { session ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (session.isActive) stringResource(R.string.home_active_session, session.name) else session.name,
                                            fontWeight = if (session.isActive) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    },
                                    onClick = {
                                        haptics.confirm()
                                        viewModel.switchSession(session.id)
                                        menuExpanded = false
                                    },
                                )
                            }
                            if (uiState.allSessions.isNotEmpty()) {
                                androidx.compose.material3.HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.sessions_manage),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                },
                                onClick = {
                                    haptics.tap()
                                    menuExpanded = false
                                    onManageSessions()
                                },
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptics.tap()
                    uiState.activeSession?.let { onAddSubject(it.id) }
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_subject)) },
                modifier = Modifier.semantics { contentDescription = context.getString(R.string.home_add_subject_cd) }
            )
        }
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Gradient header card with attendance + period selector
                    item {
                        OverallAttendanceCard(
                            subjects = uiState.subjects,
                            sessionName = uiState.activeSession?.name ?: "",
                            sessionStart = uiState.activeSession?.startDate ?: 0L,
                            sessionEnd = uiState.activeSession?.endDate ?: 0L,
                            overallAttended = uiState.overallAttended,
                            overallTotal = uiState.overallTotal,
                            monthlyAttended = uiState.monthlyAttended,
                            monthlyTotal = uiState.monthlyTotal,
                            weeklyAttended = uiState.weeklyAttended,
                            weeklyTotal = uiState.weeklyTotal,
                        )
                    }

                    // Subject list with staggered animation
                    itemsIndexed(uiState.subjects, key = { _, s -> s.subject.id }) { index, subjectWithAtt ->
                        val delayMs = if (animations) index * 40 else 0
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = delayMs)) +
                                slideInVertically(tween(300, delayMillis = delayMs)) { it / 4 },
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
                            )
                        }
                    }

                }
            }
        }
    }

    // Attendance marking bottom sheet
    markingSubject?.let { subject ->
        AttendanceMarkSheet(
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

@Composable
private fun OverallAttendanceCard(
    subjects: List<SubjectWithAttendance>,
    sessionName: String,
    sessionStart: Long,
    sessionEnd: Long,
    overallAttended: Int,
    overallTotal: Int,
    monthlyAttended: Int,
    monthlyTotal: Int,
    weeklyAttended: Int,
    weeklyTotal: Int,
) {
    val accentPreset = LocalAccentPreset.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    var selectedPeriod by remember { mutableStateOf(0) }
    val haptics = LocalHaptics.current

    // Compute period-specific attendance
    val (attended, total) = when (selectedPeriod) {
        1 -> monthlyAttended to monthlyTotal
        2 -> weeklyAttended to weeklyTotal
        else -> overallAttended to overallTotal
    }
    val pct = if (total > 0) (attended * 100 / total) else 0
    val hasData = total > 0

    // Dynamic color: green when safe, red when below 75%, neutral when no data
    // Always use white/light text on the gradient header for consistent contrast
    val targetPct = 75
    val percentageColor = when {
        !hasData -> Color.White.copy(alpha = 0.6f)
        pct >= targetPct -> Color(0xFFB6F500)
        pct < targetPct -> Color(0xFFFF6B6B)
        else -> Color.White
    }

    val periodLabel = when (selectedPeriod) {
        1 -> stringResource(R.string.home_period_this_month)
        2 -> stringResource(R.string.home_period_this_week)
        else -> stringResource(R.string.home_period_label_overall)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(primaryGradient(accentPreset))
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = sessionName.ifBlank { stringResource(R.string.home_period_label_overall) },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Period selector chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    stringResource(R.string.home_period_overall),
                    stringResource(R.string.home_period_monthly),
                    stringResource(R.string.home_period_weekly),
                ).forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPeriod == index,
                        onClick = {
                            haptics.tap()
                            selectedPeriod = index
                        },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White.copy(alpha = 0.2f),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White.copy(alpha = 0.7f),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasData) stringResource(R.string.home_period_label, periodLabel, pct) else "$periodLabel: --%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = percentageColor,
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (hasData) {
                Text(
                    text = "$attended / $total classes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (sessionStart > 0 && sessionEnd > 0) {
                Text(
                    text = "${dateFormat.format(Date(sessionStart))} — ${dateFormat.format(Date(sessionEnd))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            val subjectCount = subjects.size
            Text(
                text = stringResource(
                    if (subjectCount == 1) R.string.home_subject_count
                    else R.string.home_subject_count_plural,
                    subjectCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}
