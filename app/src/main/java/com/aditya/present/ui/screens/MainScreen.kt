package com.aditya.present.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aditya.present.ui.components.BannerAd
import com.aditya.present.ui.navigation.Tab
import com.aditya.present.ui.theme.LocalAccentPreset
import com.aditya.present.ui.theme.LocalHaptics
import com.aditya.present.ui.theme.PillShape
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onAddSubject: (Long) -> Unit,
    onEditSubject: (Long, Long) -> Unit = { _, _ -> },
    onAbout: () -> Unit = {},
    onSessions: () -> Unit = {},
    onStats: () -> Unit = {},
    onBunkCalculator: (Long, String, Int, Int, Int, Float) -> Unit = { _, _, _, _, _, _ -> },
) {
    val tabs = Tab.entries
    val haptics = LocalHaptics.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })

    var lastPage by remember { mutableStateOf(0) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != lastPage) {
            haptics.tap()
            lastPage = pagerState.currentPage
        }
    }

    Scaffold(
        bottomBar = {
            FloatingNavBar(
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
                beyondViewportPageCount = 1,
            ) { page ->
                when (tabs[page]) {
                    Tab.DASHBOARD -> DashboardScreen(
                        onAddSubject = onAddSubject,
                        onManageSessions = onSessions,
                        onStats = onStats,
                    )
                    Tab.SUBJECTS -> SubjectsScreen(
                        onAddSubject = onAddSubject,
                        onEditSubject = onEditSubject,
                        onBunkCalculator = onBunkCalculator,
                    )
                    Tab.CALENDAR -> CalendarScreen()
                    Tab.TIMETABLE -> TimetableScreen()
                    Tab.SETTINGS -> SettingsScreen(onAbout = onAbout, onStats = onStats)
                }
            }
            BannerAd()
        }
    }
}

@Composable
private fun FloatingNavBar(
    tabs: List<Tab>,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
    val accentPreset = LocalAccentPreset.current
    val isDark = LocalConfiguration.current.uiMode and
        android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES

    val barColor = if (isDark)
        MaterialTheme.colorScheme.surfaceContainerHigh
    else
        MaterialTheme.colorScheme.surface

    val pillGradient = Brush.horizontalGradient(
        listOf(accentPreset.gradientStart, accentPreset.gradientEnd),
    )

    val pillShape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 12.dp else 8.dp,
                    shape = PillShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                )
                .clip(PillShape)
                .background(barColor)
                .height(72.dp),
        ) {
            val tabWidth = maxWidth / tabs.size
            val pillWidth = 48.dp
            val pillHeight = 32.dp

            val selectedIndex = tabs.indexOfFirst { it == selectedTab }.coerceAtLeast(0)

            val indicatorOffset by animateDpAsState(
                targetValue = (tabWidth * selectedIndex) + (tabWidth - pillWidth) / 2,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "PillSlider",
            )

            // Indicator pill — sits behind the icon only, above the label
            Box(
                modifier = Modifier
                    .offset(
                        x = indicatorOffset,
                        y = 10.dp,
                    )
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(pillShape)
                    .background(pillGradient),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab

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
                        verticalArrangement = Arrangement.Top,
                    ) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = stringResource(tab.labelRes),
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(tab.labelRes),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold
                            else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
