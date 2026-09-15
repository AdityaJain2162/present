package com.aditya.present.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.components.SubjectCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubject: () -> Unit,
    onSubjectClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    modifier = Modifier.fillMaxSize().padding(padding),
                    onAddSubject = onAddSubject,
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.subjects, key = { it.id }) { subject ->
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

@Composable
private fun EmptyState(
    modifier: Modifier,
    onAddSubject: () -> Unit,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "P",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to Present!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first subject to start tracking attendance and never worry about falling below your target again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddSubject, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Add Your First Subject")
        }
    }
}
