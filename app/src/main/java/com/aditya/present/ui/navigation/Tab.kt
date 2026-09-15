package com.aditya.present.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.graphics.vector.ImageVector

enum class Tab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("tab_home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    CALENDAR("tab_calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    TIMETABLE("tab_timetable", "Timetable", Icons.Filled.TableChart, Icons.Outlined.TableChart),
    SETTINGS("tab_settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}
