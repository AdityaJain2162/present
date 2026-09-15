package com.aditya.present.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.aditya.present.R
import com.aditya.present.domain.AttendanceStatus
import com.aditya.present.ui.theme.LocalHaptics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceMarkSheet(
    subjectName: String,
    subjectColor: Int,
    teacherName: String,
    todaySlots: List<SlotInfo> = emptyList(),
    onMark: (AttendanceStatus) -> Unit,
    onMarkSlot: (Long, AttendanceStatus) -> Unit = { _, status -> onMark(status) },
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = LocalHaptics.current
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Subject header with color dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(subjectColor)),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subjectName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    if (teacherName.isNotBlank()) {
                        Text(
                            text = teacherName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (todaySlots.size > 1) {
                // Per-slot marking: show each slot with its time
                Text(
                    text = stringResource(R.string.mark_each_slot),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                todaySlots.forEach { slot ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = slot.timeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.width(72.dp),
                        )
                        SlotStatusChips(
                            currentStatus = slot.currentStatus,
                            onMark = { status ->
                                haptics.heavy()
                                onMarkSlot(slot.slotId, status)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // Single mark: show all 5 status buttons
                Text(
                    text = stringResource(R.string.mark_today_attendance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 5 action buttons: Present, Absent in row 1; Cancelled, On-Duty, Holiday in row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MarkButton(
                        icon = Icons.Filled.Check,
                        label = stringResource(R.string.status_present),
                        gradient = Brush.linearGradient(listOf(Color(0xFF43A047), Color(0xFF2E7D32))),
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = context.getString(R.string.mark_as_present)
                        },
                        onClick = {
                            haptics.heavy()
                            onMark(AttendanceStatus.PRESENT)
                        },
                    )
                    MarkButton(
                        icon = Icons.Filled.Close,
                        label = stringResource(R.string.status_absent),
                        gradient = Brush.linearGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828))),
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = context.getString(R.string.mark_as_absent)
                        },
                        onClick = {
                            haptics.heavy()
                            onMark(AttendanceStatus.ABSENT)
                        },
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MarkButton(
                        icon = Icons.Filled.EventBusy,
                        label = stringResource(R.string.status_cancelled),
                        gradient = Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFEF6C00))),
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = context.getString(R.string.mark_as_cancelled)
                        },
                        onClick = {
                            haptics.confirm()
                            onMark(AttendanceStatus.CANCELLED)
                        },
                    )
                    MarkButton(
                        icon = Icons.Filled.Work,
                        label = stringResource(R.string.status_on_duty),
                        gradient = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        ),
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = context.getString(R.string.mark_as_on_duty)
                        },
                        onClick = {
                            haptics.confirm()
                            onMark(AttendanceStatus.ON_DUTY)
                        },
                    )
                    MarkButton(
                        icon = Icons.Filled.BeachAccess,
                        label = stringResource(R.string.status_holiday),
                        gradient = Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFF6A1B9A))),
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = context.getString(R.string.mark_as_holiday)
                        },
                        onClick = {
                            haptics.confirm()
                            onMark(AttendanceStatus.HOLIDAY)
                        },
                    )
                }
            }
        }
    }
}

data class SlotInfo(
    val slotId: Long,
    val timeText: String,
    val currentStatus: AttendanceStatus? = null,
)

@Composable
private fun SlotStatusChips(
    currentStatus: AttendanceStatus?,
    onMark: (AttendanceStatus) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AttendanceStatus.entries.forEach { status ->
            val isSelected = currentStatus == status
            val labelRes = when (status) {
                AttendanceStatus.PRESENT -> R.string.status_present
                AttendanceStatus.ABSENT -> R.string.status_absent
                AttendanceStatus.CANCELLED -> R.string.status_cancelled
                AttendanceStatus.HOLIDAY -> R.string.status_holiday
                AttendanceStatus.ON_DUTY -> R.string.status_on_duty
            }
            FilterChip(
                selected = isSelected,
                onClick = { onMark(status) },
                label = { Text(stringResource(labelRes), fontSize = 10.sp) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MarkButton(
    icon: ImageVector,
    label: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "ButtonScale",
    )

    Column(
        modifier = modifier
            .heightIn(min = 96.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
