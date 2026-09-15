package com.aditya.present.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aditya.present.ui.components.BannerAd
import com.aditya.present.ui.navigation.Tab
import com.aditya.present.ui.theme.LocalHaptics
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onAddSubject: (Long) -> Unit,
    onAbout: () -> Unit = {},
    onSessions: () -> Unit = {},
) {
    val tabs = Tab.entries
    val haptics = LocalHaptics.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })

    // Haptic feedback when page changes via swipe
    var lastPage by remember { mutableStateOf(0) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != lastPage) {
            haptics.tap()
            lastPage = pagerState.currentPage
        }
    }

    Scaffold(
        bottomBar = {
            FluidSlidingNavBar(
                tabs = tabs,
                selectedTab = tabs[pagerState.currentPage],
                onTabSelected = { tab ->
                    haptics.tap()
                    val index = tabs.indexOf(tab)
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                beyondViewportPageCount = 0,
            ) { page ->
                when (tabs[page]) {
                    Tab.HOME -> HomeScreen(
                        onAddSubject = onAddSubject,
                        onSubjectClick = { },
                        onManageSessions = onSessions,
                    )
                    Tab.CALENDAR -> CalendarScreen()
                    Tab.TIMETABLE -> TimetableScreen()
                    Tab.SETTINGS -> SettingsScreen(onAbout = onAbout)
                }
            }
            // Pinned banner ad — always visible across all 4 main tabs
            BannerAd()
        }
    }
}

@Composable
private fun FluidSlidingNavBar(
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(barColor)
                .height(60.dp),
        ) {
            val tabWidth = maxWidth / tabs.size
            // Pill covers most of the tab width and full height minus padding
            val pillWidth = (tabWidth - 8.dp).coerceAtLeast(48.dp)
            val pillHeight = 44.dp

            val selectedIndex = tabs.indexOfFirst { it == selectedTab }.coerceAtLeast(0)

            val indicatorOffset by animateDpAsState(
                targetValue = (tabWidth * selectedIndex) + ((tabWidth - pillWidth) / 2),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                label = "PillSlider",
            )

            // Sliding pill indicator — covers full tab area
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset, y = (60.dp - pillHeight) / 2)
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val haptics = LocalHaptics.current

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(tab) },
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
