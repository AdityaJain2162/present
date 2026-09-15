package com.aditya.present.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aditya.present.ui.screens.AboutScreen
import com.aditya.present.ui.screens.AddEditSubjectScreen
import com.aditya.present.ui.screens.BunkCalculatorScreen
import com.aditya.present.ui.screens.MainScreen
import com.aditya.present.ui.screens.OnboardingScreen
import com.aditya.present.ui.screens.OnboardingViewModel
import com.aditya.present.ui.screens.SessionsScreen
import com.aditya.present.ui.screens.StatsScreen

import java.net.URLEncoder
import java.net.URLDecoder

object Routes {
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val ABOUT = "about"
    const val SESSIONS = "sessions"
    const val STATS = "stats"
    const val ADD_SUBJECT = "add_subject/{sessionId}"
    const val EDIT_SUBJECT = "edit_subject/{sessionId}/{subjectId}"
    const val BUNK_CALCULATOR = "bunk/{subjectId}/{subjectName}/{subjectColor}/{attended}/{total}/{target}"

    fun addSubject(sessionId: Long) = "add_subject/$sessionId"
    fun editSubject(sessionId: Long, subjectId: Long) = "edit_subject/$sessionId/$subjectId"
    fun bunkCalculator(
        subjectId: Long,
        subjectName: String,
        subjectColor: Int,
        attended: Int,
        total: Int,
        target: Float,
    ): String {
        val encodedName = URLEncoder.encode(subjectName, "UTF-8")
        return "bunk/$subjectId/$encodedName/$subjectColor/$attended/$total/$target"
    }
}

@Composable
fun PresentNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val hasSession by onboardingViewModel.hasActiveSession.collectAsState()

    // Show loading until we know whether a session exists
    if (hasSession == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (hasSession == true) Routes.MAIN else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onSessionCreated = { type, name, start, end, target ->
                    onboardingViewModel.createSession(type, name, start, end, target) { sessionId ->
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onAddSubject = { sessionId ->
                    navController.navigate(Routes.addSubject(sessionId))
                },
                onEditSubject = { sessionId, subjectId ->
                    navController.navigate(Routes.editSubject(sessionId, subjectId))
                },
                onAbout = {
                    navController.navigate(Routes.ABOUT)
                },
                onSessions = {
                    navController.navigate(Routes.SESSIONS)
                },
                onStats = {
                    navController.navigate(Routes.STATS)
                },
                onBunkCalculator = { subjectId, name, color, attended, total, target ->
                    navController.navigate(
                        Routes.bunkCalculator(subjectId, name, color, attended, total, target)
                    )
                },
            )
        }

        composable(Routes.SESSIONS) {
            SessionsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.STATS) {
            StatsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ADD_SUBJECT,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 1L
            AddEditSubjectScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDIT_SUBJECT,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.LongType },
                navArgument("subjectId") { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 1L
            val subjectId = backStackEntry.arguments?.getLong("subjectId")
            AddEditSubjectScreen(
                sessionId = sessionId,
                subjectId = subjectId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.BUNK_CALCULATOR,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.LongType },
                navArgument("subjectName") { type = NavType.StringType },
                navArgument("subjectColor") { type = NavType.IntType },
                navArgument("attended") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("target") { type = NavType.FloatType },
            ),
        ) { backStackEntry ->
            val rawName = backStackEntry.arguments?.getString("subjectName") ?: ""
            val decodedName = URLDecoder.decode(rawName, "UTF-8")
            BunkCalculatorScreen(
                subjectName = decodedName,
                subjectColor = backStackEntry.arguments?.getInt("subjectColor") ?: 0xFF6750A4.toInt(),
                attendedUnits = backStackEntry.arguments?.getInt("attended") ?: 0,
                totalUnits = backStackEntry.arguments?.getInt("total") ?: 0,
                targetPercent = backStackEntry.arguments?.getFloat("target") ?: 75f,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
