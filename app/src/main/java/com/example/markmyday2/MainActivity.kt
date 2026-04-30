package com.example.markmyday2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.markmyday2.ui.navigation.Screen
import com.example.markmyday2.ui.screens.*
import com.example.markmyday2.ui.theme.MarkMYDay2Theme
import com.example.markmyday2.ui.viewmodel.AuthViewModel
import com.example.markmyday2.ui.viewmodel.UserState
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Safe Firebase Initialization
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:dummy") // Placeholder ID
                    .setProjectId("markmyday-dummy")
                    .setApiKey("dummy-api-key")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Firebase Init Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        enableEdgeToEdge()
        setContent {
            MarkMYDay2Theme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val userState = authViewModel.userState.collectAsState().value

    // Handle initial redirection if already logged in
    androidx.compose.runtime.LaunchedEffect(userState) {
        if (userState is UserState.Authenticated) {
            val destination = when (userState.user.role) {
                com.example.markmyday2.data.model.UserRole.ADMIN -> Screen.AdminDashboard.route
                com.example.markmyday2.data.model.UserRole.TEACHER -> Screen.TeacherDashboard.route
                com.example.markmyday2.data.model.UserRole.STUDENT -> Screen.StudentDashboard.route
            }
            // Navigate and clear backstack to prevent going back to login
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    when (role) {
                        "ADMIN" -> navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        "TEACHER" -> navController.navigate(Screen.TeacherDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        "STUDENT" -> navController.navigate(Screen.StudentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.TeacherDashboard.route) {
            val userState = authViewModel.userState.collectAsState().value
            if (userState is UserState.Authenticated) {
                TeacherDashboard(
                    teacherId = userState.user.userId,
                    onClassClick = { classId ->
                        navController.navigate(Screen.MarkAttendance.createRoute(classId))
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.TeacherDashboard.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Screen.MarkAttendance.route) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId") ?: ""
            val userState = authViewModel.userState.collectAsState().value
            if (userState is UserState.Authenticated) {
                MarkAttendanceScreen(
                    classId = classId,
                    teacherId = userState.user.userId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.StudentDashboard.route) {
            val userState = authViewModel.userState.collectAsState().value
            if (userState is UserState.Authenticated) {
                StudentDashboard(
                    studentId = userState.user.userId,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.StudentDashboard.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
