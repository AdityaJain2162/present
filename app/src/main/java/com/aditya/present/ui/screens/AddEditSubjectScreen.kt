package com.aditya.present.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.theme.LocalHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubjectScreen(
    sessionId: Long,
    subjectId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditSubjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = LocalHaptics.current

    LaunchedEffect(subjectId) {
        if (subjectId != null) {
            viewModel.loadSubjectById(subjectId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEdit) "Edit Subject" else "Add Subject")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Subject name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.teacherName,
                onValueChange = viewModel::updateTeacherName,
                label = { Text("Teacher name (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.acronym.isNotBlank()) {
                Text(
                    text = "Acronym: ${uiState.acronym}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text("Color")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                val colors = listOf(
                    0xFF006A6A.toInt(), 0xFF0061A4.toInt(), 0xFF6750A4.toInt(),
                    0xFFB3261E.toInt(), 0xFFE6654F.toInt(), 0xFF7D5700.toInt(),
                )
                colors.forEach { color ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                            .clickable {
                                haptics.tap()
                                viewModel.updateColor(color)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.color == color) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected",
                                tint = Color.White)
                        }
                    }
                }
            }

            Text("Target attendance: ${uiState.targetPercent.toInt()}%")
            Slider(
                value = uiState.targetPercent,
                onValueChange = viewModel::updateTargetPercent,
                valueRange = 50f..100f,
                steps = 9,
            )

            Button(
                onClick = {
                    haptics.confirm()
                    viewModel.save(sessionId, subjectId)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.name.isNotBlank(),
            ) {
                Text(if (uiState.isEdit) "Save" else "Add Subject")
            }
        }
    }
}
