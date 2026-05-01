package com.project.markmyday.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.project.markmyday.ui.screens.*
import com.project.markmyday.ui.auth.AuthenticationScreen

sealed class Screen(val route: String) {
    object Authentication : Screen("auth")
    object RoleSelector : Screen("role_selector")
    object StudentDashboard : Screen("student_dashboard/{name}/{role}")
    object TeacherDashboard : Screen("teacher_dashboard/{name}/{role}")
    object AdminDashboard : Screen("admin_dashboard/{name}/{role}")
    object Notifications : Screen("notifications")
    object Admissions : Screen("admissions")
    object AddStaff : Screen("add_staff")
    
    // Bottom Bar Screens
    object Happenings : Screen("happenings")
    object Learning : Screen("SKP")
    object Reports : Screen("marks")
    object GlobalUpdates : Screen("global_updates")
    object Login : Screen("login")
}

@Composable
fun AppNavigation(startDestination: String = Screen.Authentication.route) {
    val navController = rememberNavController()
    
    // Track the last dashboard route to know where "Home" should go
    // Use rememberSaveable to survive configuration changes
    var lastDashboardRoute by rememberSaveable { mutableStateOf(Screen.StudentDashboard.route) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Authentication.route) {
            AuthenticationScreen(onLoginSuccess = { name, role ->
                // Routing logic based on user role
                val baseRoute = when (role.lowercase()) {
                    "principal", "headmaster", "admin" -> "admin_dashboard"
                    "teacher" -> "teacher_dashboard"
                    "student" -> "student_dashboard"
                    else -> "student_dashboard" // Default fallback
                }

                val destination = "$baseRoute/$name/$role"
                lastDashboardRoute = destination

                navController.navigate(destination) {
                    popUpTo(Screen.Authentication.route) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }

        composable(Screen.RoleSelector.route) {
            RoleSelectorScreen(onRoleSelected = { role ->
                // Navigate to Login (AuthenticationScreen will show LoginContent)
                navController.navigate(Screen.Authentication.route)
            })
        }

        // Screen.Login is no longer used as a separate route
        
        composable(
            Screen.StudentDashboard.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: "Student"
            val role = backStackEntry.arguments?.getString("role") ?: "Student"
            
            StudentDashboard(
                userName = name,
                userRole = role,
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
        
        composable(
            Screen.TeacherDashboard.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: "Teacher"
            val role = backStackEntry.arguments?.getString("role") ?: "Teacher"

            TeacherDashboard(
                userName = name,
                userRole = role,
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
        
        composable(
            Screen.AdminDashboard.route,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = backStackEntry.arguments?.getString("name") ?: "Admin"
            val role = backStackEntry.arguments?.getString("role") ?: "Administrator"

            AdminDashboard(
                userName = name,
                userRole = role,
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
