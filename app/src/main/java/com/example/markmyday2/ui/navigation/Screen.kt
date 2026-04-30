package com.example.markmyday2.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentDashboard : Screen("student_dashboard")
    object CreateUser : Screen("create_user")
    object Register : Screen("register")
    object MarkAttendance : Screen("mark_attendance/{classId}") {
        fun createRoute(classId: String) = "mark_attendance/$classId"
    }
}
