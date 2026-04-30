package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry

@Composable
fun TeacherDashboard(
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit
) {
    val teacherTiles = listOf(
        DashboardTile("attendance", "Mark Attendance", Icons.Default.Checklist, badgeText = "80%"),
        DashboardTile("assignments", "Assignments", Icons.AutoMirrored.Filled.Assignment, badgeCount = 12),
        DashboardTile("results", "Post Results", Icons.Default.Description),
        DashboardTile("leave", "Leave Request", Icons.Default.EventBusy),
        DashboardTile("exams", "Manage Exams", Icons.Default.Quiz, badgeCount = 2),
        DashboardTile("messages", "Messages", Icons.AutoMirrored.Filled.Chat, badgeCount = 5)
    )

    val timetable = listOf(
        TimetableEntry("Mobile Dev", "CS402", "09:00 - 10:00 AM", "Room 302"),
        TimetableEntry("UI/UX Design", "DS101", "10:30 - 11:30 AM", "Lab 1"),
        TimetableEntry("Data Science", "CS505", "01:00 - 02:00 PM", "Room 105")
    )

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Teacher Dashboard",
                onNotificationClick = onNotificationClick,
                notificationCount = 8
            )
        },
        bottomBar = {
            DashboardBottomBar(currentRoute = "dashboard", onNavigate = {})
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            WelcomeSection(name = "Dr. Anuj Sharma", role = "Senior Faculty")
            
            TimetableSection(entries = timetable)
            
            DashboardTileGrid(tiles = teacherTiles, onTileClick = { onTileClick(it.id) })
        }
    }
}
