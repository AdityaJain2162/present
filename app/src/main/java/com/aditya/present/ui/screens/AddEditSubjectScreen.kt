package com.aditya.present.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.components.BannerAd
import com.aditya.present.ui.components.PhysicsButton
import com.aditya.present.ui.theme.CardShape
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
        subjectId?.let { viewModel.loadSubjectById(it) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    val titleRes = if (uiState.isEdit) R.string.edit_subject_title else R.string.add_subject_title

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Gradient hero header showing the selected color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(uiState.color), Color(uiState.color).copy(alpha = 0.6f))
                        )
                    )
                    .padding(24.dp),
            ) {
                Column {
                    Text(
                        text = if (uiState.acronym.isNotBlank()) uiState.acronym
                        else "ABC",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.name.ifBlank { stringResource(R.string.subject_name_label) },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }

            // Form card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text(stringResource(R.string.subject_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = uiState.teacherName,
                    onValueChange = viewModel::updateTeacherName,
                    label = { Text(stringResource(R.string.teacher_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (uiState.acronym.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.acronym_preview, uiState.acronym),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Color swatches with spring + haptic selection
                Text(
                    text = stringResource(R.string.color_label),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val colors = listOf(
                        0xFF006A6A.toInt(), 0xFF0061A4.toInt(), 0xFF6750A4.toInt(),
                        0xFFB3261E.toInt(), 0xFFE6654F.toInt(), 0xFF7D5700.toInt(),
                        0xFF386A20.toInt(), 0xFFBB4D72.toInt(),
                    )
                    colors.forEach { color ->
                        ColorSwatch(
                            color = color,
                            isSelected = uiState.color == color,
                            onClick = {
                                haptics.confirm()
                                viewModel.updateColor(color)
                            },
                        )
                    }
                }

                // Target attendance slider
                Text(
                    text = stringResource(R.string.target_attendance_label, uiState.targetPercent.toInt()),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = uiState.targetPercent,
                    onValueChange = viewModel::updateTargetPercent,
                    valueRange = 50f..100f,
                    steps = 9,
                )
            }

            // Save button with physics
            PhysicsButton(
                text = stringResource(if (uiState.isEdit) R.string.save_subject else R.string.add_subject_title),
                onClick = {
                    haptics.confirm()
                    viewModel.save(sessionId, subjectId)
                },
                enabled = uiState.name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AdMob banner at the bottom of Add/Edit Subject
            BannerAd()
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "swatchScale",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Transparent,
        label = "swatchBorder",
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Color(color))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.color_selected),
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
