package com.project.markmyday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import com.project.markmyday.ui.components.*
import com.project.markmyday.R
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry
import com.project.markmyday.ui.theme.MarkMyDayTheme
import androidx.compose.ui.tooling.preview.Preview

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.NotificationViewModel
import com.project.markmyday.viewmodel.EngagementViewModel

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
    val engagementViewModel: EngagementViewModel = viewModel()
    val hasUnread by notificationViewModel.hasUnreadNotices.collectAsState()
    val engagementSummaries by engagementViewModel.engagementSummaries.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userRole, homeSection) {
        notificationViewModel.fetchNotifications(userRole)
        if (homeSection != "N/A") {
            engagementViewModel.fetchEngagement(homeSection)
        }
    }

    val teacherTiles = listOf(
        DashboardTile("attendance", stringResource(R.string.tile_mark_attendance), Icons.Default.Checklist, badgeText = "80%"),
        DashboardTile("assignments", stringResource(R.string.tile_assignments), Icons.AutoMirrored.Filled.Assignment, badgeCount = 12),
        DashboardTile("results", stringResource(R.string.tile_post_results), Icons.Default.Description),
        DashboardTile("leave", stringResource(R.string.tile_leave_request), Icons.Default.HistoryEdu),
        DashboardTile("exams", stringResource(R.string.tile_manage_exams), Icons.Default.Quiz, badgeCount = 2),
        DashboardTile("updates", stringResource(R.string.tile_updates), Icons.Default.Update),
        DashboardTile("notifications", stringResource(R.string.tile_notices), Icons.Default.Notifications, badgeCount = if (hasUnread) 1 else 0),
        DashboardTile("admissions", stringResource(R.string.tile_new_admission), Icons.Default.School),
        DashboardTile("course_manager", stringResource(R.string.tile_course_manager), Icons.Default.VideoLibrary),
        DashboardTile("messages", stringResource(R.string.tile_messages), Icons.AutoMirrored.Filled.Chat, badgeCount = 5),
        DashboardTile("settings", stringResource(R.string.settings), Icons.Default.Settings)
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
                title = stringResource(R.string.teacher_hub),
                onNotificationClick = onNotificationClick,
                notificationCount = if (hasUnread) 1 else 0
            )
        },
        bottomBar = {
            DashboardBottomBar(currentRoute = "dashboard", onNavigate = onNavigate)
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Welcome Header
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hello, $userName",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$userRole • $homeSection • $subject",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Avatar",
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2. Banner
            item(span = { GridItemSpan(2) }) {
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
                                    stringResource(R.string.class_in_session),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.next_class_starts),
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
            }

            // 3. Timetable Section
            item(span = { GridItemSpan(2) }) {
                TimetableSection(entries = timetable)
            }

            // 4. Scan Card
            item(span = { GridItemSpan(2) }) {
                ScanCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onOpenScanner = {
                        context.startActivity(Intent(context, ScanActivity::class.java))
                    }
                )
            }

            // 5. Watchlist Card
            if (homeSection != "N/A") {
                item(span = { GridItemSpan(2) }) {
                    WatchlistCard(
                        summaries = engagementSummaries,
                        onExport = { engagementViewModel.exportReport(context) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // 6. Section Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = stringResource(R.string.faculty_portal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 7. Tiles
            items(teacherTiles, key = { it.id }) { tile ->
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    AnimatedDashboardTile(
                        tile = tile,
                        onClick = { onTileClick(tile.id) }
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun WatchlistCard(
    summaries: List<com.project.markmyday.data.model.StudentEngagementSummary>,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Watchlist (Today)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onExport) {
                    Icon(Icons.Default.Download, contentDescription = "Export Report", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (summaries.isEmpty()) {
                Text(
                    "No watch activity detected today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                summaries.take(5).forEach { summary ->
                    val totalSeconds = summary.videoStats.values.sumOf { it.timeSpentSeconds }
                    val hours = totalSeconds / 3600.0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(summary.studentName, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format("%.2f hrs", hours),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (summaries.size > 5) {
                    Text(
                        "and ${summaries.size - 5} more...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Daily Report")
            }
        }
    }
}

@Composable
fun ScanCard(
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mark Daily Attendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan student QR codes to record presence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onOpenScanner,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Scanner")
            }
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
