package com.aditya.present.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.domain.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
            )
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(
                            selected = themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                        )
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Present — Every class counts.\nPrivacy-first attendance tracker.\n100% local, no backend, no cloud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
