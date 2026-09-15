package com.aditya.present.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.theme.LocalAccentPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val accentPreset = LocalAccentPreset.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Overview stats
            StatsCard(title = "Overview") {
                StatRow("Overall", "${uiState.overallAttended}/${uiState.overallTotal}",
                    if (uiState.overallTotal > 0) "${(uiState.overallAttended.toFloat() / uiState.overallTotal * 100).toInt()}%" else "--%")
                StatRow("This Month", "${uiState.monthlyAttended}/${uiState.monthlyTotal}",
                    if (uiState.monthlyTotal > 0) "${(uiState.monthlyAttended.toFloat() / uiState.monthlyTotal * 100).toInt()}%" else "--%")
                StatRow("This Week", "${uiState.weeklyAttended}/${uiState.weeklyTotal}",
                    if (uiState.weeklyTotal > 0) "${(uiState.weeklyAttended.toFloat() / uiState.weeklyTotal * 100).toInt()}%" else "--%")
            }

            // Streak stats
            StatsCard(title = "Streaks") {
                StatRow("Current Streak", "${uiState.currentStreak} days", "")
                StatRow("Best Streak", "${uiState.bestStreak} days", "")
                StatRow("Perfect Days", "${uiState.perfectDays}", "")
            }

            // Per-subject breakdown
            StatsCard(title = "Subject Breakdown") {
                uiState.subjects.forEach { subject ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(subject.subject.color)),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = subject.subject.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (subject.totalUnits > 0) "${(subject.percentage * 100).toInt()}%" else "--%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (subject.percentage >= subject.subject.targetAttendancePercent / 100f)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${subject.attendedUnits}/${subject.totalUnits}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Attendance trend chart (last 14 days)
            val trendData = computeTrendData(uiState.subjects.flatMap { listOf(it) })
            StatsCard(title = "14-Day Attendance Trend") {
                TrendChart(
                    data = trendData,
                    color = accentPreset.gradientStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, percentage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (percentage.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Simple line chart showing attendance percentage over the last 14 days.
 * Each point is the cumulative attendance percentage up to that day.
 */
@Composable
private fun TrendChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 10f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        val stepX = chartWidth / (data.size - 1)

        // Draw grid lines at 25%, 50%, 75%, 100%
        for (pct in listOf(0.25f, 0.5f, 0.75f, 1f)) {
            val y = padding + chartHeight * (1 - pct)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f,
            )
        }

        // Draw the line path
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = padding + chartHeight * (1 - value.coerceIn(0f, 1f))
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        // Fill gradient under the line
        val fillPath = Path().apply {
            addPath(path)
            lineTo(padding + (data.size - 1) * stepX, padding + chartHeight)
            lineTo(padding, padding + chartHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                startY = padding,
                endY = padding + chartHeight,
            ),
        )

        // Draw the line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3f, cap = StrokeCap.Round),
        )

        // Draw points
        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = padding + chartHeight * (1 - value.coerceIn(0f, 1f))
            drawCircle(
                color = color,
                radius = 4f,
                center = Offset(x, y),
            )
        }
    }
}

/**
 * Compute a simple trend: for each of the last 14 days, calculate the
 * cumulative attendance percentage up to and including that day.
 */
private fun computeTrendData(subjects: List<SubjectWithAttendance>): List<Float> {
    // This is a simplified placeholder — returns empty for now
    // A full implementation would query attendance by day from the DAO
    return emptyList()
}
