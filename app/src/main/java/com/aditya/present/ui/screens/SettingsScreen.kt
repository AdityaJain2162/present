package com.aditya.present.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.domain.ThemeMode
import com.aditya.present.ui.theme.AccentPresets
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.primaryGradient

private val GITHUB_URL = "https://github.com/AdityaJain2162"
private val LINKEDIN_URL = "https://www.linkedin.com/in/adityajain2162/"
private val REPO_URL = "https://github.com/AdityaJain2162/present"

private val ALL_DAYS = listOf(
    "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.themePrefs.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Appearance ──
            SettingsCard(title = "Appearance") {
                Column(modifier = Modifier.selectableGroup()) {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                        ) {
                            RadioButton(
                                selected = prefs.mode == mode,
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
            }

            // ── Accent Color ──
            SettingsCard(title = "Accent Color") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(AccentPresets) { preset ->
                        AccentSwatch(
                            preset = preset,
                            isSelected = prefs.accentName == preset.name,
                            onClick = { viewModel.setAccentName(preset.name) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic color", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Android 12+ — blends with wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = prefs.dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                    )
                }
            }

            // ── Behavior ──
            SettingsCard(title = "Behavior") {
                ToggleRow(
                    title = "Smooth animations",
                    subtitle = "Transitions, spring motion, staggered lists",
                    checked = prefs.animationsEnabled,
                    onCheckedChange = { viewModel.setAnimationsEnabled(it) },
                )
                Spacer(modifier = Modifier.height(12.dp))
                ToggleRow(
                    title = "Haptic feedback",
                    subtitle = "Vibration on button presses and marking",
                    checked = prefs.hapticFeedback,
                    onCheckedChange = { viewModel.setHapticFeedback(it) },
                )
            }

            // ── Attendance Defaults ──
            SettingsCard(title = "Attendance Defaults") {
                Text(
                    "Default target: ${prefs.defaultTargetPercent}%",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = prefs.defaultTargetPercent.toFloat(),
                    onValueChange = { viewModel.setDefaultTargetPercent(it.toInt()) },
                    valueRange = 50f..100f,
                    steps = 9,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Auto-mark time: ${prefs.autoMarkHour}:00",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    "Unmarked classes auto-mark as Present at this hour",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Slider(
                    value = prefs.autoMarkHour.toFloat(),
                    onValueChange = { viewModel.setAutoMarkHour(it.toInt()) },
                    valueRange = 18f..23f,
                    steps = 5,
                )
            }

            // ── Weekend Config ──
            SettingsCard(title = "Weekend Days") {
                Text(
                    "These days are auto-excluded from attendance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val selectedDays = prefs.weekendDays.split(",").filter { it.isNotBlank() }.toSet()
                    ALL_DAYS.forEach { day ->
                        FilterChip(
                            selected = selectedDays.contains(day),
                            onClick = { viewModel.toggleWeekendDay(day) },
                            label = {
                                Text(day.take(3).replaceFirstChar { it.uppercase() })
                            },
                        )
                    }
                }
            }

            // ── Customization ──
            SettingsCard(title = stringResource(R.string.settings_customization)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Card Style
                    Column {
                        Text(
                            stringResource(R.string.settings_card_style),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            stringResource(R.string.settings_card_style_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Filled" to "filled", "Outlined" to "outlined", "Elevated" to "elevated").forEach { (label, value) ->
                                FilterChip(
                                    selected = prefs.cardStyle == value,
                                    onClick = { viewModel.setCardStyle(value) },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Show Percentage
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_show_percentage),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.settings_show_percentage_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = prefs.showPercentageOnCards,
                            onCheckedChange = { viewModel.setShowPercentageOnCards(it) },
                        )
                    }

                    HorizontalDivider()

                    // Compact Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.settings_compact_mode),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                stringResource(R.string.settings_compact_mode_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = prefs.compactMode,
                            onCheckedChange = { viewModel.setCompactMode(it) },
                        )
                    }

                    HorizontalDivider()

                    // Haptic Feedback
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Haptic Feedback",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                "Vibrate on taps and actions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = prefs.hapticFeedback,
                            onCheckedChange = { viewModel.setHapticFeedback(it) },
                        )
                    }
                }
            }

            // ── About (navigates to full About screen) ──
            SettingsCard(title = stringResource(R.string.settings_about)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .clickable(onClick = onAbout)
                        .padding(vertical = 8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Present — Every class counts.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "Developer info, links, privacy, license",
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccentSwatch(
    preset: com.aditya.present.ui.theme.AccentPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.animateContentSize(),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(primaryGradient(preset))
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun android.content.Context.openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
