package com.project.markmyday.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
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
    object Admissions : Screen("admissions")
    object AddStaff : Screen("add_staff")
    
    // Bottom Bar Screens
    object Happenings : Screen("happenings")
    object Learning : Screen("SKP")
    object Reports : Screen("marks")
    object GlobalUpdates : Screen("global_updates")
}

@Composable
fun AppNavigation(startDestination: String = Screen.RoleSelector.route) {
    val navController = rememberNavController()
    
    // Track the last dashboard route to know where "Home" should go
    // Use rememberSaveable to survive configuration changes
    var lastDashboardRoute by rememberSaveable { mutableStateOf(Screen.StudentDashboard.route) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.RoleSelector.route) {
            RoleSelectorScreen(onRoleSelected = { role ->
                val destination = when (role) {
                    "student" -> Screen.StudentDashboard.route
                    "teacher" -> Screen.TeacherDashboard.route
                    "admin" -> Screen.AdminDashboard.route
                    else -> Screen.StudentDashboard.route
                }
                lastDashboardRoute = destination
                navController.navigate(destination) {
                    // Don't pop inclusive if the user wants to go back to "Navigation screen" (RoleSelector)
                    // But usually, dashboards are root. Let's keep it for now as per "Navigation screen" hint.
                    // Actually, let's NOT pop inclusive to allow back to RoleSelector.
                }
            })
        }
        
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onTileClick = { id ->
                    when (id) {
                        "updates" -> navController.navigate(Screen.GlobalUpdates.route)
                        "results" -> navController.navigate(Screen.Reports.route)
                        "notifications" -> navController.navigate(Screen.Notifications.route)
                    }
                },
                onNavigate = { route -> handleBottomNav(route, navController, lastDashboardRoute) }
            )
        }
        
        composable(Screen.TeacherDashboard.route) {
            TeacherDashboard(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onTileClick = { id ->
                    when (id) {
                        "updates" -> navController.navigate(Screen.GlobalUpdates.route)
                        // Add more routing as screens are implemented
                    }
                },
                onNavigate = { route -> handleBottomNav(route, navController, lastDashboardRoute) }
            )
        }
        
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onTileClick = { id -> 
                    when (id) {
                        "admissions" -> navController.navigate(Screen.Admissions.route)
                        "updates" -> navController.navigate(Screen.GlobalUpdates.route)
                        "reports" -> navController.navigate(Screen.Reports.route)
                        "add_staff" -> navController.navigate(Screen.AddStaff.route)
                        // Add more routing as screens are implemented
                    }
                },
                onNavigate = { route -> handleBottomNav(route, navController, lastDashboardRoute) }
            )
        }
        
        composable(Screen.AddStaff.route) {
            AddStaffScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Admissions.route) {
            AdmissionsScreen(
                role = "Administrator",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(onBackClick = { navController.popBackStack() })
        }

        // New Bottom Bar Routes
        composable(Screen.Happenings.route) {
            HappeningsScreen(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onNavigate = { route -> handleBottomNav(route, navController, lastDashboardRoute) }
            )
        }

        composable(Screen.Learning.route) {
            LearningScreen(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onNavigate = { route -> handleBottomNav(route, navController, lastDashboardRoute) }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNotificationClick = { navController.navigate(Screen.Notifications.route) },
                onNavigate = { route -> handleBottomNav(route, navController, lastDashboardRoute) }
            )
        }

        composable(Screen.GlobalUpdates.route) {
            GlobalUpdateScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun handleBottomNav(route: String, navController: NavHostController, lastDashboardRoute: String) {
    val destination = if (route == "dashboard") lastDashboardRoute else route
    
    if (navController.currentBackStackEntry?.destination?.route == destination) {
        return
    }

    navController.navigate(destination) {
        // Pop up to the dashboard route if it's in the stack. 
        // We use popUpTo with the route name directly.
        // If it's not found, Navigation usually handles it by not popping.
        popUpTo(lastDashboardRoute) {
            saveState = true
        }
        
        launchSingleTop = true
        restoreState = true
    }
}
