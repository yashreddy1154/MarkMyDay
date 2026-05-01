package com.project.markmyday.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry
import androidx.compose.ui.tooling.preview.Preview
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.R


@Composable
fun StudentDashboard(
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit
) {
    var currentScreen by remember { mutableStateOf("student_dashboard") }

    when (currentScreen) {
        "student_dashboard" -> {
            StudentDashboardHome(
                onNotificationClick = onNotificationClick,
                onTileClick = { id ->
                    when (id) {
                        "attendance" -> currentScreen = "attendance"
                        "leave" -> currentScreen = "leave"
                        else -> onTileClick(id)
                    }
                }
            )
        }
        "attendance" -> {
            AttendanceScreen(
                onBack = { currentScreen = "student_dashboard" }
            )
        }
        "leave" -> {
            LeaveScreen(
                onBack = { currentScreen = "student_dashboard" },
                onApply = { 
                    // logic for leave application can be added here later
                    currentScreen = "student_dashboard"
                }
            )
        }
    }
}

@Composable
fun StudentDashboardHome(
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit
)
{
        val studentTiles = listOf(
        DashboardTile("attendance", "Attendance", Icons.Default.CalendarMonth, badgeText = "85%"),
        DashboardTile("assignments", "Assignments", Icons.Default.Task, badgeCount = 3),
        DashboardTile("results", "My Results", Icons.Default.Grade),
            DashboardTile("leave", "Apply Leave", Icons.Default.HistoryEdu),
        DashboardTile("exams", "Upcoming Exams", Icons.Default.History, badgeCount = 1),
        DashboardTile("fees", "Fee Statement", Icons.Default.Payments)
    )

    val timetable = listOf(
        TimetableEntry("Mathematics", "MA101", "09:00 - 10:00 AM", "Room 101"),
        TimetableEntry("Physics", "PH102", "11:00 - 12:00 PM", "Lab 2"),
        TimetableEntry("English", "EN103", "02:00 - 03:00 PM", "Room 205"),
        TimetableEntry("Telugu", "TU101", "04:00 - 05:00 PM", "Room 10")
    )


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                icon = Icons.Default.Face,
                title = "My School Life",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WelcomeSection(name = "Rahul Kumar", role = "Class 1 - A")
                    
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "App Icon",
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Student banner
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Keep Learning! 🚀",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "You have 3 assignments due soon.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TimetableSection(entries = timetable)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "My Dashboard",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            DashboardTileGrid(tiles = studentTiles, onTileClick = { onTileClick(it.id) })
            
            Spacer(modifier = Modifier.height(24.dp))
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
