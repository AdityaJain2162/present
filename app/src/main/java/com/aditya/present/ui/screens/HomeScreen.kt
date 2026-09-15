package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.components.EmptyState
import com.aditya.present.ui.components.SubjectCard
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalAnimationsEnabled
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
                    Column {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold)
                        uiState.activeSession?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
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
                    // Gradient header card with overall attendance
                    item {
                        OverallAttendanceCard(
                            subjectCount = uiState.subjects.size,
                            sessionName = uiState.activeSession?.name ?: "",
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
) {
    val accentPreset = LocalAccentPreset.current

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
            Text(
                text = "--%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$subjectCount ${if (subjectCount == 1) "subject" else "subjects"} • Tap a subject to mark attendance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )
        }
    }
}


