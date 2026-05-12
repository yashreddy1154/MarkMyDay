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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
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
    val timetableViewModel: com.project.markmyday.viewmodel.TimetableViewModel = viewModel()
    
    val hasUnread by notificationViewModel.hasUnreadNotices.collectAsState()
    val engagementSummaries by engagementViewModel.engagementSummaries.collectAsState()
    val timetables by timetableViewModel.allTimetables.collectAsState()
    val allTeachers by timetableViewModel.allTeachers.collectAsState()
    
    val context = LocalContext.current

    LaunchedEffect(userRole, homeSection) {
        notificationViewModel.fetchNotifications(userRole)
        if (homeSection != "N/A") {
            engagementViewModel.fetchEngagement(homeSection)
        }
    }

    // Identify current teacher ID
    val teacherId = remember(allTeachers, userName) {
        allTeachers.find { it.name.equals(userName, ignoreCase = true) }?.teacherId ?: ""
    }

    // Get today's classes for this teacher
    val today = remember {
        val calendar = java.util.Calendar.getInstance()
        when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Monday"
            java.util.Calendar.TUESDAY -> "Tuesday"
            java.util.Calendar.WEDNESDAY -> "Wednesday"
            java.util.Calendar.THURSDAY -> "Thursday"
            java.util.Calendar.FRIDAY -> "Friday"
            java.util.Calendar.SATURDAY -> "Saturday"
            else -> ""
        }
    }

    val teacherTodayEntries = remember(timetables, teacherId, today) {
        val entries = mutableListOf<TimetableEntry>()
        if (teacherId.isNotEmpty()) {
            timetables.forEach { timetable ->
                val daySched = timetable.weeklySchedule[today]
                daySched?.periods?.filter { it.teacherId == teacherId }?.forEach { period ->
                    entries.add(
                        TimetableEntry(
                            subject = period.subject,
                            code = "P${period.periodNumber}",
                            time = "${period.startTime} - ${period.endTime}",
                            room = timetable.className // Use class name as "room" for teacher
                        )
                    )
                }
            }
        }
        entries.sortedBy { it.code }
    }

    var searchQuery by remember { mutableStateOf("") }

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
    ).filter { it.label.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                icon = Icons.Default.CastForEducation,
                title = stringResource(R.string.teacher_hub),
                onNotificationClick = onNotificationClick,
                notificationCount = if (hasUnread) 1 else 0,
                onProfileClick = { onTileClick("settings") }
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
                WelcomeSection(
                    name = userName,
                    role = "$homeSection • $subject",
                    icon = Icons.Default.CastForEducation
                )
            }

            // 1.5 Search Bar
            item(span = { GridItemSpan(2) }) {
                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            }

            // 2. Banner
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.class_in_session),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.next_class_starts),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Timetable Section
            item(span = { GridItemSpan(2) }) {
                TeacherTimetableSection(
                    entries = teacherTodayEntries,
                    onViewAllClick = {
                        val intent = Intent(context, TeacherTimetableActivity::class.java).apply {
                            putExtra("teacherName", userName)
                        }
                        context.startActivity(intent)
                    }
                )
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
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Watchlist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = onExport,
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Export Report", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (summaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No activity detected today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                summaries.take(5).forEach { summary ->
                    val totalSeconds = summary.videoStats.values.sumOf { it.timeSpentSeconds }
                    val hours = totalSeconds / 3600.0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = if (hours > 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                        ) {}
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            summary.studentName, 
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            String.format("%.2f hrs", hours),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
                
                if (summaries.size > 5) {
                    Text(
                        "and ${summaries.size - 5} more students...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Detailed Insights", fontWeight = FontWeight.Bold)
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
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Attendance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Quick scan student QR codes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = onOpenScanner,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Launch Scanner", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
