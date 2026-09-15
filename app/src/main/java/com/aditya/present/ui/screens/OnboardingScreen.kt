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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.aditya.present.R
import com.aditya.present.domain.SessionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onSessionCreated: (SessionType, String, Long, Long, Float) -> Unit,
) {
    var step by remember { mutableStateOf(0) }
    var sessionType by remember { mutableStateOf<SessionType?>(null) }
    var sessionName by remember { mutableStateOf("") }

    Scaffold { padding ->
        when (step) {
            0 -> WelcomeStep(
                modifier = Modifier.padding(padding),
                onContinue = { step = 1 },
            )
            1 -> SessionTypeStep(
                modifier = Modifier.padding(padding),
                onSelected = {
                    sessionType = it
                    sessionName = if (it == SessionType.SEMESTER) "Semester 1" else "Year 1"
                    step = 2
                },
                onBack = { step = 0 },
            )
            2 -> SessionDetailsStep(
                modifier = Modifier.padding(padding),
                sessionType = sessionType ?: SessionType.SEMESTER,
                sessionName = sessionName,
                onNameChange = { sessionName = it },
                onCreate = {
                    val now = System.currentTimeMillis()
                    val sixMonths = 1000L * 60 * 60 * 24 * 180
                    onSessionCreated(
                        sessionType ?: SessionType.SEMESTER,
                        sessionName.ifBlank { "Session 1" },
                        now,
                        now + sixMonths,
                        75f,
                    )
                },
                onBack = { step = 1 },
            )
        }
    }
}

@Composable
private fun WelcomeStep(modifier: Modifier, onContinue: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "P",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Every class counts.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Track attendance, calculate safe bunks, manage your timetable, and never miss a deadline — all offline, no account needed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Get Started")
        }
    }
}

@Composable
private fun SessionTypeStep(
    modifier: Modifier,
    onSelected: (SessionType) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "How do you track attendance?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            onClick = { onSelected(SessionType.SEMESTER) },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "College / Semester",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track attendance semester-wise with CGPA support",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            onClick = { onSelected(SessionType.YEARLY) },
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "School / Yearly",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Track attendance for the full academic year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
private fun SessionDetailsStep(
    modifier: Modifier,
    sessionType: SessionType,
    sessionName: String,
    onNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
) {
    val typeLabel = if (sessionType == SessionType.SEMESTER) "Semester" else "Year"
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Name your $typeLabel",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(32.dp))
        androidx.compose.material3.OutlinedTextField(
            value = sessionName,
            onValueChange = onNameChange,
            label = { Text("$typeLabel name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Target attendance: 75%\nDates: Today — 6 months from now\n(You can change these later in Settings)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Create $typeLabel")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}
