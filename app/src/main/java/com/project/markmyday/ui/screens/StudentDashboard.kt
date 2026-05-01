package com.project.markmyday.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    userName: String = "Rahul Kumar",
    userRole: String = "Class 1 - A",
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    var currentSubScreen by remember { mutableStateOf("home") }

    // Handle back button to go back to home if we are in a sub-screen
    BackHandler(enabled = currentSubScreen != "home") {
        currentSubScreen = "home"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            when (currentSubScreen) {
                "home" -> DashboardTopBar(
                    icon = Icons.Default.Face,
                    title = "My School Life",
                    onNotificationClick = onNotificationClick,
                    notificationCount = 12
                )
                "attendance" -> TopAppBar(
                    title = { Text("My Attendance", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { currentSubScreen = "home" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                "leave" -> TopAppBar(
                    title = { Text("Apply Leave", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { currentSubScreen = "home" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        bottomBar = {
            DashboardBottomBar(currentRoute = "dashboard", onNavigate = onNavigate)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (currentSubScreen) {
                "home" -> StudentDashboardHomeContent(
                    userName = userName,
                    userRole = userRole,
                    onTileClick = { id ->
                        when (id) {
                            "attendance" -> currentSubScreen = "attendance"
                            "leave" -> currentSubScreen = "leave"
                            else -> onTileClick(id)
                        }
                    }
                )
                "attendance" -> AttendanceScreenContent()
                "leave" -> LeaveScreenContent(onApply = { currentSubScreen = "home" })
            }
        }
    }
}

@Composable
fun StudentDashboardHomeContent(
    userName: String,
    userRole: String,
    onTileClick: (String) -> Unit
) {
    val studentTiles = listOf(
        DashboardTile("attendance", "Attendance", Icons.Default.CalendarMonth, badgeText = "85%"),
        DashboardTile("assignments", "Assignments", Icons.Default.Task, badgeCount = 3),
        DashboardTile("results", "My Results", Icons.Default.Grade),
        DashboardTile("leave", "Apply Leave", Icons.Default.HistoryEdu),
        DashboardTile("updates", "Global Updates", Icons.Default.Update),
        DashboardTile("exams", "Upcoming Exams", Icons.Default.History, badgeCount = 1),
        DashboardTile("fees", "Fee Statement", Icons.Default.Payments),
        DashboardTile("settings", "Settings", Icons.Default.Settings)
    )

    val timetable = listOf(
        TimetableEntry("Mathematics", "MA101", "09:00 - 10:00 AM", "Room 101"),
        TimetableEntry("Physics", "PH102", "11:00 - 12:00 PM", "Lab 2"),
        TimetableEntry("English", "EN103", "02:00 - 03:00 PM", "Room 205"),
        TimetableEntry("Telugu", "TU101", "04:00 - 05:00 PM", "Room 10")
    )

    Column(
        modifier = Modifier
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
                WelcomeSection(name = userName, role = userRole)

                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.surface,
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

@Preview(showBackground = true)
@Composable
fun StudentDashboardPreview() {
    MarkMyDayTheme {
        StudentDashboard(onNotificationClick = {}, onTileClick = {}, onNavigate = {})
    }
}
