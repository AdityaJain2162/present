package com.aditya.present.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
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
    onAbout: () -> Unit = {},
    onSessions: () -> Unit = {},
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
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
    else
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)

    val pillGradient = Brush.horizontalGradient(
        listOf(accentPreset.gradientStart, accentPreset.gradientEnd),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 8.dp else 6.dp,
                    shape = PillShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                )
                .clip(PillShape)
                .background(barColor)
                .height(68.dp),
        ) {
            val tabWidth = maxWidth / tabs.size
            val pillWidth = tabWidth
            val pillHeight = 52.dp

            val selectedIndex = tabs.indexOfFirst { it == selectedTab }.coerceAtLeast(0)

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "PillSlider",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset, y = (68.dp - pillHeight) / 2)
                    .width(pillWidth)
                    .height(pillHeight)
                    .clip(RoundedCornerShape(24.dp))
                    .background(pillGradient),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val haptics = LocalHaptics.current

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                        label = "IconScale",
                    )

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
                            modifier = Modifier
                                .size(24.dp)
                                .scale(iconScale),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.SemiBold
                            else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
