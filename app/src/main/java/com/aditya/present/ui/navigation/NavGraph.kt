package com.aditya.present.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aditya.present.ui.screens.AddEditSubjectScreen
import com.aditya.present.ui.screens.HomeScreen

object Routes {
    const val HOME = "home"
    const val ADD_SUBJECT = "add_subject/{sessionId}"
    const val EDIT_SUBJECT = "edit_subject/{sessionId}/{subjectId}"

    fun addSubject(sessionId: Long) = "add_subject/$sessionId"
    fun editSubject(sessionId: Long, subjectId: Long) = "edit_subject/$sessionId/$subjectId"
}

@Composable
fun PresentNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddSubject = {
                    // TODO: pass actual session ID once onboarding creates a session
                    navController.navigate(Routes.addSubject(1L))
                },
                onSubjectClick = { subjectId ->
                    // TODO: navigate to subject detail
                },
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
    }
}
