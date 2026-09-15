package com.aditya.present.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onMark: (AttendanceStatus) -> Unit,
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
                .padding(bottom = 32.dp),
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
            Text(
                text = stringResource(R.string.mark_today_attendance),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 4 action buttons in 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MarkButton(
                    icon = Icons.Filled.Check,
                    label = stringResource(R.string.status_present),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
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
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
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
                    containerColor = Color(0xFFFF9800),
                    contentColor = Color.White,
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f).semantics {
                        contentDescription = context.getString(R.string.mark_as_on_duty)
                    },
                    onClick = {
                        haptics.confirm()
                        onMark(AttendanceStatus.ON_DUTY)
                    },
                )
            }
        }
    }
}

@Composable
private fun MarkButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
    }
}
