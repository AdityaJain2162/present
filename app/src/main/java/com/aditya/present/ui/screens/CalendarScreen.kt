package com.aditya.present.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aditya.present.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Calendar") }) }
    ) { padding ->
        EmptyState(
            icon = Icons.Filled.CalendarMonth,
            title = "Calendar View",
            subtitle = "See your attendance history month by month with color-coded days.",
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}
