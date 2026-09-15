package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.aditya.present.R
import com.aditya.present.domain.SessionType
import com.aditya.present.ui.components.BannerAd
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalAnimationsEnabled
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.primaryGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onSessionCreated: (SessionType, String, Long, Long, Float) -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    var sessionType by remember { mutableStateOf<SessionType?>(null) }
    var sessionName by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember {
        mutableStateOf(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 180)
    }
    val accentPreset = LocalAccentPreset.current
    val animations = LocalAnimationsEnabled.current
    val haptics = LocalHaptics.current

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Progress indicator
            if (step > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= step) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                                ),
                        )
                    }
                }
            }
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (!animations) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                else slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300)) togetherWith
                    slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200))
            },
            label = "onboarding",
            modifier = Modifier.weight(1f),
        ) { currentStep ->
            when (currentStep) {
                0 -> WelcomeStep(onContinue = {
                    haptics.tap()
                    step = 1
                })
                1 -> SessionTypeStep(
                    onSelected = {
                        haptics.confirm()
                        sessionType = it
                        sessionName = if (it == SessionType.SEMESTER) "Semester 1" else "Year 1"
                        step = 2
                    },
                    onBack = {
                        haptics.tap()
                        step = 0
                    },
                )
                2 -> SessionDetailsStep(
                    sessionType = sessionType ?: SessionType.SEMESTER,
                    sessionName = sessionName,
                    startDate = startDate,
                    endDate = endDate,
                    onNameChange = { sessionName = it },
                    onStartDateChange = { startDate = it },
                    onEndDateChange = { endDate = it },
                    onCreate = {
                        haptics.confirm()
                        onSessionCreated(
                            sessionType ?: SessionType.SEMESTER,
                            sessionName.ifBlank { "Session 1" },
                            startDate,
                            endDate,
                            75f,
                        )
                    },
                    onBack = {
                        haptics.tap()
                        step = 1
                    },
                )
            }
        }

            // AdMob banner at the bottom of Onboarding
            BannerAd()
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    val accentPreset = LocalAccentPreset.current

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Gradient logo circle
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(primaryGradient(accentPreset)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "P",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Feature highlights
        FeatureHighlightRow(
            icon = "✓",
            title = "Track Attendance",
            desc = "Mark Present, Absent, Cancelled & more",
        )
        Spacer(modifier = Modifier.height(12.dp))
        FeatureHighlightRow(
            icon = "📅",
            title = "Smart Calendar",
            desc = "Monthly view with color-coded days",
        )
        Spacer(modifier = Modifier.height(12.dp))
        FeatureHighlightRow(
            icon = "🔒",
            title = "100% Private",
            desc = "No account, no cloud, fully offline",
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_get_started), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun FeatureHighlightRow(icon: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.size(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SessionTypeStep(
    onSelected: (SessionType) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.onboarding_track_question),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(36.dp))

        // College / Semester card with gradient
        Card(
            onClick = { onSelected(SessionType.SEMESTER) },
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_college_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_college_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // School / Yearly card
        Card(
            onClick = { onSelected(SessionType.YEARLY) },
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.onboarding_school_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.onboarding_school_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetailsStep(
    sessionType: SessionType,
    sessionName: String,
    startDate: Long,
    endDate: Long,
    onNameChange: (String) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
) {
    val typeLabel = if (sessionType == SessionType.SEMESTER) "Semester" else "Year"
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onStartDateChange(it) }
                    showStartPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    if (showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onEndDateChange(it) }
                    showEndPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(
                if (sessionType == SessionType.SEMESTER) R.string.onboarding_name_semester
                else R.string.onboarding_name_year
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(36.dp))
        OutlinedTextField(
            value = sessionName,
            onValueChange = onNameChange,
            label = {
                Text(
                    stringResource(
                        if (sessionType == SessionType.SEMESTER) R.string.onboarding_semester_name
                        else R.string.onboarding_year_name
                    )
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Start date picker button
        OutlinedButton(
            onClick = { showStartPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.onboarding_start_date, dateFormat.format(Date(startDate))),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // End date picker button
        OutlinedButton(
            onClick = { showEndPicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.onboarding_end_date, dateFormat.format(Date(endDate))),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_target_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(
                    if (sessionType == SessionType.SEMESTER) R.string.onboarding_create_semester
                    else R.string.onboarding_create_year
                ),
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
    }
}
