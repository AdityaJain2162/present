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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.components.SubjectCard
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalAnimationsEnabled
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.primaryGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubject: () -> Unit,
    onSubjectClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accentPreset = LocalAccentPreset.current
    val animations = LocalAnimationsEnabled.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    val haptics = LocalHaptics.current

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
                                        text = uiState.activeSession?.name ?: "No session",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (uiState.allSessions.size > 1) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = "Switch session",
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
                                            text = session.name + if (session.isActive) " (active)" else "",
                                            fontWeight = if (session.isActive) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        haptics.confirm()
                                        viewModel.switchSession(session.id)
                                        menuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddSubject,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_subject)) },
                modifier = Modifier.semantics { contentDescription = "Add subject" }
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
                    title = "Welcome to Present!",
                    subtitle = "Add your first subject to start tracking attendance and never worry about falling below your target again.",
                    modifier = Modifier.fillMaxSize().padding(padding),
                    action = {
                        Button(onClick = onAddSubject, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Add Your First Subject", fontWeight = FontWeight.Medium)
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
                            subjectCount = uiState.subjects.size,
                            sessionName = uiState.activeSession?.name ?: "",
                            sessionStart = uiState.activeSession?.startDate ?: 0L,
                            sessionEnd = uiState.activeSession?.endDate ?: 0L,
                        )
                    }

                    // Subject list with staggered animation
                    itemsIndexed(uiState.subjects, key = { _, s -> s.id }) { index, subject ->
                        val delayMs = if (animations) index * 40 else 0
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300, delayMillis = delayMs)) +
                                slideInVertically(tween(300, delayMillis = delayMs)) { it / 4 },
                        ) {
                            SubjectCard(
                                subject = subject,
                                attendedUnits = 0,
                                totalUnits = 0,
                                percentage = 0f,
                                onClick = { onSubjectClick(subject.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallAttendanceCard(
    subjectCount: Int,
    sessionName: String,
    sessionStart: Long,
    sessionEnd: Long,
) {
    val accentPreset = LocalAccentPreset.current
    val dateFormat = remember { java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()) }
    var selectedPeriod by remember { mutableStateOf(0) } // 0=Overall, 1=Monthly, 2=Weekly
    val haptics = com.aditya.present.ui.theme.LocalHaptics.current

    val periodLabel = when (selectedPeriod) {
        1 -> "This Month"
        2 -> "This Week"
        else -> "Overall"
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
                text = sessionName.ifBlank { "Overall" },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Period selector chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Overall", "Monthly", "Weekly").forEachIndexed { index, label ->
                    androidx.compose.material3.FilterChip(
                        selected = selectedPeriod == index,
                        onClick = {
                            haptics.tap()
                            selectedPeriod = index
                        },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$periodLabel: --%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (sessionStart > 0 && sessionEnd > 0) {
                Text(
                    text = "${dateFormat.format(java.util.Date(sessionStart))} — ${dateFormat.format(java.util.Date(sessionEnd))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "$subjectCount ${if (subjectCount == 1) "subject" else "subjects"} • Tap a subject to mark attendance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
        }
    }
}


