package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            FloatingGlassNavBar(
                tabs = Tab.entries,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
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

@Composable
private fun FloatingGlassNavBar(
    tabs: List<Tab>,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val barColor = if (isDark)
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
    else
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.75f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(barColor)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    isDark = isDark,
                    onClick = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavItem(
    tab: Tab,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indicatorAlpha = if (selected) 0.15f else 0f
    val indicatorColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .drawBehind {
                    if (indicatorAlpha > 0f) {
                        drawRoundRect(
                            color = indicatorColor.copy(alpha = indicatorAlpha),
                            cornerRadius = CornerRadius(16.dp.toPx()),
                        )
                    }
                }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                modifier = Modifier.size(24.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontSize = 11.sp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
