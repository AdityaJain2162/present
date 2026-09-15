package com.aditya.present.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aditya.present.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Timetable") }) }
    ) { padding ->
        EmptyState(
            icon = Icons.Filled.TableChart,
            title = "Timetable",
            subtitle = "Set up your weekly class schedule and see today's classes at a glance.",
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}
