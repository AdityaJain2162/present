package com.aditya.present.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aditya.present.R
import com.aditya.present.domain.BunkCalculator
import com.aditya.present.ui.components.BannerAd
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.primaryGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BunkCalculatorScreen(
    subjectName: String,
    subjectColor: Int,
    attendedUnits: Int,
    totalUnits: Int,
    targetPercent: Float,
    onBack: () -> Unit,
) {
    var futureClasses by remember { mutableFloatStateOf(5f) }
    var futureBunks by remember { mutableFloatStateOf(0f) }

    val target = targetPercent / 100f
    val projectedAttended = attendedUnits + futureClasses.toInt()
    val projectedTotal = totalUnits + futureClasses.toInt() + futureBunks.toInt()
    val projectedPct = if (projectedTotal > 0) projectedAttended.toFloat() / projectedTotal else 0f

    val result = BunkCalculator.calculate(
        attendedUnits = attendedUnits,
        totalUnits = totalUnits,
        targetPercent = targetPercent,
        remainingClasses = futureClasses.toInt(),
    )

    val accentPreset = LocalAccentPreset.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.subject_bunk_calculator)) },
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
            // Subject header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(subjectColor)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subjectName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Current stats card
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
                    Text(stringResource(R.string.bunk_current_attendance), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = if (totalUnits > 0) "${(result.percentage * 100).toInt()}%" else "--%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (result.percentage >= target)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = stringResource(R.string.bunk_classes, attendedUnits, totalUnits),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        StatLabel(stringResource(R.string.bunk_target), "${targetPercent.toInt()}%")
                        StatLabel(stringResource(R.string.bunk_safe_bunks), "${result.safeBunks}")
                        StatLabel(stringResource(R.string.bunk_recovery), "${result.recoveryNeeded}")
                    }
                }
            }

            // Future scenario simulator
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
                    Text(stringResource(R.string.bunk_future_scenario), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.bunk_future_scenario_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Future classes to attend
                    Text(
                        stringResource(R.string.bunk_classes_attend, futureClasses.toInt()),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Slider(
                        value = futureClasses,
                        onValueChange = { futureClasses = it },
                        valueRange = 0f..30f,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Future classes to bunk
                    Text(
                        stringResource(R.string.bunk_classes_bunk, futureBunks.toInt()),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Slider(
                        value = futureBunks,
                        onValueChange = { futureBunks = it },
                        valueRange = 0f..30f,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Projected result
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (projectedPct >= target)
                                    Brush.horizontalGradient(listOf(Color(0xFF43A047), Color(0xFF2E7D32)))
                                else
                                    Brush.horizontalGradient(listOf(Color(0xFFEF5350), Color(0xFFC62828)))
                            )
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.bunk_projected),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                )
                                Text(
                                    text = "${(projectedPct * 100).toInt()}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    stringResource(R.string.bunk_classes, projectedAttended, projectedTotal),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                )
                            }
                            Icon(
                                imageVector = if (projectedPct >= target) Icons.Filled.CheckCircle else Icons.Filled.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status message
                    val statusText = when {
                        projectedTotal == 0 -> stringResource(R.string.bunk_no_classes)
                        projectedPct >= target -> stringResource(
                            R.string.bunk_safe_status,
                            BunkCalculator.classesCanBunk(projectedAttended, projectedTotal, target),
                        )
                        else -> stringResource(
                            R.string.bunk_need_attend,
                            BunkCalculator.classesToAttend(projectedAttended, projectedTotal, target),
                        )
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (projectedPct >= target)
                            Color(0xFF2E7D32)
                        else
                            MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
            BannerAd()
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
