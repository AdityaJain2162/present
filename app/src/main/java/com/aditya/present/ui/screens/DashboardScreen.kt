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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.aditya.present.R
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.components.LowAttendanceBanner
import com.aditya.present.ui.components.LowAttendanceSubject
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddSubject: (Long) -> Unit,
    onManageSessions: () -> Unit = {},
    onStats: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accentPreset = LocalAccentPreset.current
    val haptics = LocalHaptics.current

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    var menuExpanded by remember { mutableStateOf(false) }

                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable {
                                    haptics.tap()
                                    menuExpanded = true
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.activeSession?.name ?: stringResource(R.string.home_no_session),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = stringResource(R.string.home_switch_session),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
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
                                HorizontalDivider()
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
            )
        },
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = {
                    haptics.tap()
                    uiState.activeSession?.let { onAddSubject(it.id) }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_add_subject)) },
            )
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
                            sessionTargetPercent = uiState.activeSession?.targetAttendancePercent ?: 75f,
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

                    // Quick stats link
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptics.tap()
                                    onStats()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.dashboard_view_stats),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = stringResource(R.string.dashboard_view_stats_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
