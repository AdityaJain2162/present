package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.aditya.present.ui.components.LowAttendanceBanner
import com.aditya.present.ui.components.LowAttendanceSubject
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

enum class SubjectFilter(val label: String) {
    ALL("All"),
    BELOW_TARGET("Below Target"),
    MARKED_TODAY("Marked Today"),
    UNMARKED("Unmarked"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubject: (Long) -> Unit,
    onSubjectClick: (Long) -> Unit,
    onManageSessions: () -> Unit = {},
    onBunkCalculator: (Long, String, Int, Int, Int, Float) -> Unit = { _, _, _, _, _, _ -> },
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
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(SubjectFilter.ALL) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .clickable { menuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
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
                                            modifier = Modifier.size(18.dp),
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
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                haptics.tap()
                                uiState.activeSession?.let { onAddSubject(it.id) }
                            }
                            .semantics { contentDescription = context.getString(R.string.home_add_subject_cd) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
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

                    // Streak stats row
                    item {
                        StreakStatsRow(
                            currentStreak = uiState.currentStreak,
                            bestStreak = uiState.bestStreak,
                            perfectDays = uiState.perfectDays,
                        )
                    }

                    // Low-attendance warning banner
                    item {
                        val lowSubjects = uiState.subjects
                            .filter { it.totalUnits > 0 && it.percentage < (it.subject.targetAttendancePercent / 100f) }
                            .sortedBy { it.percentage }
                            .map {
                                LowAttendanceSubject(
                                    name = it.subject.name,
                                    color = it.subject.color,
                                    percentage = it.percentage,
                                    target = it.subject.targetAttendancePercent,
                                )
                            }
                        LowAttendanceBanner(subjects = lowSubjects)
                    }

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
fun OverallAttendanceCard(
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

@Composable
fun StreakStatsRow(
    currentStreak: Int,
    bestStreak: Int,
    perfectDays: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StreakChip(
            icon = Icons.Filled.LocalFireDepartment,
            label = stringResource(R.string.home_streak_current),
            value = "$currentStreak",
            modifier = Modifier.weight(1f),
        )
        StreakChip(
            icon = Icons.Filled.EmojiEvents,
            label = stringResource(R.string.home_streak_best),
            value = "$bestStreak",
            modifier = Modifier.weight(1f),
        )
        StreakChip(
            icon = Icons.Filled.Star,
            label = stringResource(R.string.home_streak_perfect),
            value = "$perfectDays",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StreakChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
