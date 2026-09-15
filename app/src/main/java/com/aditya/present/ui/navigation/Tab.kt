package com.aditya.present.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.aditya.present.R

enum class Tab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    DASHBOARD("tab_dashboard", R.string.tab_label_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SUBJECTS("tab_subjects", R.string.tab_label_subjects, Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    CALENDAR("tab_calendar", R.string.tab_label_calendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    TIMETABLE("tab_timetable", R.string.tab_label_timetable, Icons.Filled.TableChart, Icons.Outlined.TableChart),
    SETTINGS("tab_settings", R.string.tab_label_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}
