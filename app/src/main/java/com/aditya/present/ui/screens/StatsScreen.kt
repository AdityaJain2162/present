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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditya.present.R
import com.aditya.present.ui.components.BannerAd
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
                title = { Text(stringResource(R.string.stats_title)) },
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
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            // Overview stats
            StatsCard(title = stringResource(R.string.stats_overview)) {
                StatRow(stringResource(R.string.home_period_overall), "${uiState.overallAttended}/${uiState.overallTotal}",
                    if (uiState.overallTotal > 0) "${(uiState.overallAttended.toFloat() / uiState.overallTotal * 100).toInt()}%" else "--%")
                StatRow(stringResource(R.string.home_period_this_month), "${uiState.monthlyAttended}/${uiState.monthlyTotal}",
                    if (uiState.monthlyTotal > 0) "${(uiState.monthlyAttended.toFloat() / uiState.monthlyTotal * 100).toInt()}%" else "--%")
                StatRow(stringResource(R.string.home_period_this_week), "${uiState.weeklyAttended}/${uiState.weeklyTotal}",
                    if (uiState.weeklyTotal > 0) "${(uiState.weeklyAttended.toFloat() / uiState.weeklyTotal * 100).toInt()}%" else "--%")
            }

            // Streak stats
            StatsCard(title = stringResource(R.string.stats_streaks)) {
                StatRow(stringResource(R.string.stats_current_streak), stringResource(R.string.stats_days, uiState.currentStreak), "")
                StatRow(stringResource(R.string.stats_best_streak), stringResource(R.string.stats_days, uiState.bestStreak), "")
                StatRow(stringResource(R.string.stats_perfect_days), stringResource(R.string.stats_days, uiState.perfectDays), "")
            }

            // Per-subject breakdown
            StatsCard(title = stringResource(R.string.stats_subject_breakdown)) {
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
            StatsCard(title = stringResource(R.string.stats_trend_title)) {
                TrendChart(
                    data = uiState.trendData,
                    color = accentPreset.gradientStart,
                    targetPercent = uiState.activeSession?.targetAttendancePercent ?: 75f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
            }
        }
            BannerAd()
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
 * Each point is the per-day attendance percentage for that day.
 */
@Composable
private fun TrendChart(
    data: List<Float>,
    color: Color,
    targetPercent: Float = 75f,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.stats_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    if (data.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.stats_insufficient_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val targetColor = Color(0xFFEF4444).copy(alpha = 0.6f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 36f
        val chartWidth = width - padding - 10f
        val chartHeight = height - 2 * padding

        val stepX = chartWidth / (data.size - 1)

        // Draw grid lines with labels at 25%, 50%, 75%, 100%
        for (pct in listOf(0.25f, 0.5f, 0.75f, 1f)) {
            val y = padding + chartHeight * (1 - pct)
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(padding, y),
                end = Offset(width - 10f, y),
                strokeWidth = 1f,
            )
            drawIntoCanvas {
                it.nativeCanvas.drawText(
                    "${(pct * 100).toInt()}%",
                    4f,
                    y + 8f,
                    android.graphics.Paint().apply {
                        this.color = labelColor.toArgb()
                        textSize = 20f
                    },
                )
            }
        }

        // Draw target line
        val targetY = padding + chartHeight * (1 - targetPercent / 100f)
        drawLine(
            color = targetColor,
            start = Offset(padding, targetY),
            end = Offset(width - 10f, targetY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
        )

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
