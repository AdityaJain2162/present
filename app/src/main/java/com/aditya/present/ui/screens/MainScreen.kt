package com.aditya.present.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.aditya.present.ui.navigation.Tab

@Composable
fun MainScreen(
    onAddSubject: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(Tab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == tab) tab.selectedIcon
                                else tab.unselectedIcon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            Tab.HOME -> HomeScreen(
                onAddSubject = onAddSubject,
                onSubjectClick = { },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            Tab.CALENDAR -> CalendarScreen()
            Tab.TIMETABLE -> TimetableScreen()
            Tab.SETTINGS -> SettingsScreen()
        }
    }
}
