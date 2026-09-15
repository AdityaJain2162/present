package com.aditya.present.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aditya.present.R
import com.aditya.present.data.SubjectEntity
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.theme.CardShape
import com.aditya.present.ui.theme.LocalCardStyle
import com.aditya.present.ui.theme.LocalCompactMode
import com.aditya.present.ui.theme.LocalShowPercentageOnCards

@Composable
fun SubjectCard(
    subject: SubjectEntity,
    attendedUnits: Int,
    totalUnits: Int,
    percentage: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    todayStatus: AttendanceStatus? = null,
    onBunkCalculator: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val targetPct = subject.targetAttendancePercent / 100f
    val isSafe = percentage >= targetPct
    val showPercentage = LocalShowPercentageOnCards.current
    val compact = LocalCompactMode.current
    val cardPadding = if (compact) 12.dp else 16.dp
    val progressHeight = if (compact) 4.dp else 6.dp

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    )

    val cardModifier = modifier
        .fillMaxWidth()
        .animateContentSize()
        .semantics { contentDescription = "Subject ${subject.name}. ${if (todayStatus != null) "Marked ${todayStatus.name.lowercase()} today" else "Not marked today"}. Tap to mark attendance" }

    when (LocalCardStyle.current) {
        "outlined" -> OutlinedCard(
            onClick = onClick,
            modifier = cardModifier,
            shape = CardShape,
            colors = cardColors,
        ) { SubjectCardContent(subject, attendedUnits, totalUnits, percentage, isSafe, showPercentage, compact, cardPadding, progressHeight, expanded, todayStatus, onBunkCalculator, onEdit) { expanded = !expanded } }
        "elevated" -> ElevatedCard(
            onClick = onClick,
            modifier = cardModifier,
            shape = CardShape,
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        ) { SubjectCardContent(subject, attendedUnits, totalUnits, percentage, isSafe, showPercentage, compact, cardPadding, progressHeight, expanded, todayStatus, onBunkCalculator, onEdit) { expanded = !expanded } }
        else -> Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = CardShape,
            colors = cardColors,
        ) { SubjectCardContent(subject, attendedUnits, totalUnits, percentage, isSafe, showPercentage, compact, cardPadding, progressHeight, expanded, todayStatus, onBunkCalculator, onEdit) { expanded = !expanded } }
    }
}

@Composable
private fun SubjectCardContent(
    subject: SubjectEntity,
    attendedUnits: Int,
    totalUnits: Int,
    percentage: Float,
    isSafe: Boolean,
    showPercentage: Boolean,
    compact: Boolean,
    cardPadding: androidx.compose.ui.unit.Dp,
    progressHeight: androidx.compose.ui.unit.Dp,
    expanded: Boolean,
    todayStatus: AttendanceStatus?,
    onBunkCalculator: (() -> Unit)?,
    onEdit: (() -> Unit)?,
    onToggleExpand: () -> Unit,
) {
    Column(modifier = Modifier.padding(cardPadding)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                // Color dot
                Box(
                    modifier = Modifier
                        .size(if (compact) 10.dp else 12.dp)
                        .clip(CircleShape)
                        .background(Color(subject.color)),
                )
                Spacer(modifier = Modifier.width(if (compact) 8.dp else 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        style = if (compact) MaterialTheme.typography.titleSmall
                        else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = buildString {
                            append(subject.acronym)
                            append(" • $attendedUnits/$totalUnits classes")
                            if (subject.teacherName.isNotBlank()) {
                                append(" • ${subject.teacherName}")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Today's status badge
                if (todayStatus != null) {
                    StatusBadge(status = todayStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (showPercentage) {
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                    )
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(if (compact) 6.dp else 8.dp))
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(progressHeight),
            color = if (isSafe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
        )
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Target: ${subject.targetAttendancePercent.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Attended: $attendedUnits / $totalUnits classes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (totalUnits > 0) {
                val classesToSkip = BunkCalculatorHelper.classesCanBunk(
                    attended = attendedUnits,
                    total = totalUnits,
                    target = subject.targetAttendancePercent / 100f,
                )
                val classesToAttend = BunkCalculatorHelper.classesToAttend(
                    attended = attendedUnits,
                    total = totalUnits,
                    target = subject.targetAttendancePercent / 100f,
                )
                if (classesToSkip > 0) {
                    Text(
                        text = stringResource(R.string.subject_can_skip, classesToSkip),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                } else if (classesToAttend > 0) {
                    Text(
                        text = stringResource(R.string.subject_need_to_attend, classesToAttend),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            if (onBunkCalculator != null || onEdit != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onEdit != null) {
                        androidx.compose.material3.TextButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.subject_edit))
                        }
                    }
                    if (onBunkCalculator != null) {
                        androidx.compose.material3.TextButton(onClick = onBunkCalculator) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.subject_bunk_calculator))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: AttendanceStatus) {
    val (color, icon) = when (status) {
        AttendanceStatus.PRESENT -> Color(0xFF4CAF50) to Icons.Filled.Check
        AttendanceStatus.ABSENT -> Color(0xFFEF4444) to Icons.Filled.Close
        AttendanceStatus.CANCELLED -> Color(0xFFFF9800) to Icons.Filled.EventBusy
        AttendanceStatus.HOLIDAY -> Color(0xFF9E9E9E) to Icons.Filled.EventBusy
        AttendanceStatus.ON_DUTY -> MaterialTheme.colorScheme.primary to Icons.Filled.Work
    }
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.name,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
    }
}
