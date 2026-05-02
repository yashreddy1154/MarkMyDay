package com.project.markmyday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry
import com.project.markmyday.ui.theme.MarkMyDayTheme
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.NotificationViewModel

@Composable
fun TeacherDashboard(
    userName: String = "Dr. Anuj Sharma",
    userRole: String = "Teacher",
    homeSection: String = "N/A",
    subject: String = "N/A",
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val notificationViewModel: NotificationViewModel = viewModel()
    val hasUnread by notificationViewModel.hasUnreadNotices.collectAsState()

    LaunchedEffect(userRole) {
        notificationViewModel.fetchNotifications(userRole)
    }

    val teacherTiles = listOf(
        DashboardTile("attendance", "Mark Attendance", Icons.Default.Checklist, badgeText = "80%"),
        DashboardTile("assignments", "Assignments", Icons.AutoMirrored.Filled.Assignment, badgeCount = 12),
        DashboardTile("results", "Post Results", Icons.Default.Description),
        DashboardTile("leave", "Leave Request", Icons.Default.EventBusy),
        DashboardTile("exams", "Manage Exams", Icons.Default.Quiz, badgeCount = 2),
        DashboardTile("updates", "Global Updates", Icons.Default.Update),
        DashboardTile("admissions", "New  Admission", Icons.Default.School),
        DashboardTile("messages", "Messages", Icons.AutoMirrored.Filled.Chat, badgeCount = 5),
        DashboardTile("settings", "Settings", Icons.Default.Settings)
    )

    val timetable = listOf(
        TimetableEntry("Mobile Dev", "CS402", "09:00 - 10:00 AM", "Room 302"),
        TimetableEntry("UI/UX Design", "DS101", "10:30 - 11:30 AM", "Lab 1"),
        TimetableEntry("Data Science", "CS505", "01:00 - 02:00 PM", "Room 105")
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                icon = Icons.Default.CastForEducation,
                title = "Teacher Hub",
                onNotificationClick = onNotificationClick,
                notificationCount = if (hasUnread) 1 else 0
            )
        },
        bottomBar = {
            DashboardBottomBar(currentRoute = "dashboard", onNavigate = onNavigate)
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
                    WelcomeSection(
                        name = userName,
                        role = userRole,
                        homeSection = homeSection,
                        subject = subject
                    )
                    
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

            // Teacher banner
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
                                "Class In Session 👩‍🏫",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Your next class starts in 15 mins.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            Icons.Default.Timer,
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
                text = "Faculty Portal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            DashboardTileGrid(tiles = teacherTiles) { onTileClick(it.id) }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherDashboardPreview() {
    MarkMyDayTheme {
        TeacherDashboard(onNotificationClick = {}, onTileClick = {}, onNavigate = {})
    }
}
