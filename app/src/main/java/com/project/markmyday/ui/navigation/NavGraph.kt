package com.project.markmyday.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.markmyday.ui.screens.*

sealed class Screen(val route: String) {
    object RoleSelector : Screen("role_selector")
    object StudentDashboard : Screen("student_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object AdminDashboard : Screen("admin_dashboard")
    object Notifications : Screen("notifications")
}

@Composable
fun AppNavigation(startDestination: String = Screen.RoleSelector.route) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.RoleSelector.route) {
            RoleSelectorScreen(onRoleSelected = { role ->
                when (role) {
                    "student" -> navController.navigate(Screen.StudentDashboard.route)
                    "teacher" -> navController.navigate(Screen.TeacherDashboard.route)
                    "admin" -> navController.navigate(Screen.AdminDashboard.route)
                }
            })
        }
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onTileClick = { /* Handle tile click */ }
            )
        }
        composable(Screen.TeacherDashboard.route) {
            TeacherDashboard(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onTileClick = { /* Handle tile click */ }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onTileClick = { /* Handle tile click */ }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

