package com.aditya.present.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aditya.present.R
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.primaryGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SubjectFilter(val labelRes: Int) {
    ALL(R.string.subject_filter_all),
    BELOW_TARGET(R.string.subject_filter_below_target),
    MARKED_TODAY(R.string.subject_filter_marked_today),
    UNMARKED(R.string.subject_filter_unmarked),
}

@Composable
fun OverallAttendanceCard(
    subjects: List<SubjectWithAttendance>,
    sessionName: String,
    sessionStart: Long,
    sessionEnd: Long,
    sessionTargetPercent: Float = 75f,
    overallAttended: Int,
    overallTotal: Int,
    monthlyAttended: Int,
    monthlyTotal: Int,
    weeklyAttended: Int,
    weeklyTotal: Int,
) {
    val accentPreset = LocalAccentPreset.current
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    var selectedPeriod by remember { mutableStateOf(0) }
    val haptics = LocalHaptics.current

    val (attended, total) = when (selectedPeriod) {
        1 -> monthlyAttended to monthlyTotal
        2 -> weeklyAttended to weeklyTotal
        else -> overallAttended to overallTotal
    }
    val pct = if (total > 0) (attended.toFloat() * 100 / total).toInt() else 0
    val hasData = total > 0

    val targetPct = sessionTargetPercent.toInt()
    val percentageColor = when {
        !hasData -> Color.White.copy(alpha = 0.6f)
        pct >= targetPct -> Color(0xFFB6F500)
        pct < targetPct -> Color(0xFFFF6B6B)
        else -> Color.White
    }

    val periodLabel = when (selectedPeriod) {
        1 -> stringResource(R.string.home_period_this_month)
        2 -> stringResource(R.string.home_period_this_week)
        else -> stringResource(R.string.home_period_label_overall)
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(primaryGradient(accentPreset))
            .padding(24.dp),
    ) {
        Column {
            Text(
                text = sessionName.ifBlank { stringResource(R.string.home_period_label_overall) },
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    stringResource(R.string.home_period_overall),
                    stringResource(R.string.home_period_monthly),
                    stringResource(R.string.home_period_weekly),
                ).forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPeriod == index,
                        onClick = {
                            haptics.tap()
                            selectedPeriod = index
                        },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White.copy(alpha = 0.2f),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White.copy(alpha = 0.7f),
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (hasData) stringResource(R.string.home_period_label, periodLabel, pct) else "$periodLabel: --%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = percentageColor,
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (hasData) {
                Text(
                    text = "$attended / $total classes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (sessionStart > 0 && sessionEnd > 0) {
                Text(
                    text = "${dateFormat.format(Date(sessionStart))} — ${dateFormat.format(Date(sessionEnd))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            val subjectCount = subjects.size
            Text(
                text = stringResource(
                    if (subjectCount == 1) R.string.home_subject_count
                    else R.string.home_subject_count_plural,
                    subjectCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
fun StreakStatsRow(
    currentStreak: Int,
    bestStreak: Int,
    perfectDays: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StreakChip(
            icon = Icons.Filled.LocalFireDepartment,
            label = stringResource(R.string.home_streak_current),
            value = "$currentStreak",
            modifier = Modifier.weight(1f),
        )
        StreakChip(
            icon = Icons.Filled.EmojiEvents,
            label = stringResource(R.string.home_streak_best),
            value = "$bestStreak",
            modifier = Modifier.weight(1f),
        )
        StreakChip(
            icon = Icons.Filled.Star,
            label = stringResource(R.string.home_streak_perfect),
            value = "$perfectDays",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StreakChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
