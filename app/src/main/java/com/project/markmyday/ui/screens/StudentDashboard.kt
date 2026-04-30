package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry

import androidx.compose.ui.tooling.preview.Preview
import com.project.markmyday.ui.theme.MarkMyDayTheme

@Composable
fun StudentDashboard(
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit
) {
    val studentTiles = listOf(
        DashboardTile("attendance", "My Attendance", Icons.Default.CalendarMonth, badgeText = "85%"),
        DashboardTile("assignments", "Assignments", Icons.Default.Task, badgeCount = 3),
        DashboardTile("results", "My Results", Icons.Default.Grade),
        DashboardTile("leave", "Apply Leave", Icons.Default.HistoryEdu),
        DashboardTile("exams", "Upcoming Exams", Icons.Default.History, badgeCount = 1),
        DashboardTile("fees", "Fee Statement", Icons.Default.Payments)
    )

    val timetable = listOf(
        TimetableEntry("Mathematics", "MA101", "09:00 - 10:00 AM", "Room 101"),
        TimetableEntry("Physics", "PH102", "11:00 - 12:00 PM", "Lab 2"),
        TimetableEntry("English", "EN103", "02:00 - 03:00 PM", "Room 205")
    )

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Student Dashboard",
                onNotificationClick = onNotificationClick,
                notificationCount = 12
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
            WelcomeSection(name = "Rahul Kumar", role = "Class 1")
            
            TimetableSection(entries = timetable)
            
            DashboardTileGrid(tiles = studentTiles, onTileClick = { onTileClick(it.id) })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDashboardPreview() {
    MarkMyDayTheme {
        StudentDashboard(onNotificationClick = {}, onTileClick = {})
    }
}
