package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import com.aditya.present.ui.theme.LocalAnimationsEnabled

@Composable
fun MainScreen(
    onAddSubject: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(Tab.HOME) }
    val animations = LocalAnimationsEnabled.current

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
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (!animations) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                else {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    slideInHorizontally(tween(250)) { it / 4 * direction } + fadeIn(tween(250)) togetherWith
                        slideOutHorizontally(tween(200)) { -it / 4 * direction } + fadeOut(tween(150))
                }
            },
            label = "tab",
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { tab ->
            when (tab) {
                Tab.HOME -> HomeScreen(
                    onAddSubject = onAddSubject,
                    onSubjectClick = { },
                )
                Tab.CALENDAR -> CalendarScreen()
                Tab.TIMETABLE -> TimetableScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}
