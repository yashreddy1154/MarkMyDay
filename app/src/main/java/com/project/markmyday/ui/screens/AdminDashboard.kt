package com.project.markmyday.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.NotificationViewModel
import com.project.markmyday.viewmodel.TeacherViewModel
import com.project.markmyday.viewmodel.StudentViewModel
import com.project.markmyday.viewmodel.LeaveViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    userName: String = "Principal Sharma",
    userRole: String = "Administrator",
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val decodedName = remember(userName) { 
        try { java.net.URLDecoder.decode(userName, "UTF-8") } catch (e: Exception) { userName } 
    }
    val decodedRole = remember(userRole) { 
        try { java.net.URLDecoder.decode(userRole, "UTF-8") } catch (e: Exception) { userRole } 
    }

    val notificationViewModel: NotificationViewModel = viewModel()
    val teacherViewModel: TeacherViewModel = viewModel()
    val studentViewModel: StudentViewModel = viewModel()
    val leaveViewModel: LeaveViewModel = viewModel()

    val notifications by notificationViewModel.notifications.collectAsState()
    val teachers by teacherViewModel.allTeachers.collectAsState()
    val students by studentViewModel.allStudents.collectAsState()
    val leaves by leaveViewModel.allLeaves.collectAsState()

    val hasUnread by notificationViewModel.hasUnreadNotices.collectAsState()

    LaunchedEffect(userRole) {
        notificationViewModel.fetchNotifications(userRole)
    }

    var searchQuery by remember { mutableStateOf("") }

    val pendingLeavesCount = remember(leaves) {
        leaves.count { it.status == "pending" }
    }

    // Dynamic stats from ViewModels
    val stats = listOf(
        StatItem(Icons.Outlined.Badge, stringResource(R.string.tile_staff_list), teachers.size, Color(0xFF4CAF50)),
        StatItem(Icons.Outlined.People, stringResource(R.string.tile_students), students.size, Color(0xFF2196F3)),
        StatItem(Icons.Outlined.HistoryEdu, stringResource(R.string.tile_manage_leaves), pendingLeavesCount, Color(0xFFFF9800)),
        StatItem(Icons.Outlined.Campaign, stringResource(R.string.tile_notices), notifications.size, Color(0xFF9C27B0))
    )

    val adminTiles = listOf(
        DashboardTile("notices", stringResource(R.string.tile_notices), Icons.Default.Campaign),
        DashboardTile("staff_management", stringResource(R.string.tile_staff_list), Icons.Default.Badge),
        DashboardTile("attendance_overview", stringResource(R.string.tile_daily_overview), Icons.AutoMirrored.Filled.FactCheck),
        DashboardTile("create_timetable", stringResource(R.string.tile_create_timetable), Icons.Default.CalendarToday),
        DashboardTile("add_staff", stringResource(R.string.tile_add_staff), Icons.Default.PersonAdd),
        DashboardTile("add_student", stringResource(R.string.tile_add_student), Icons.Default.PersonAddAlt1),
        DashboardTile("timetable", stringResource(R.string.tile_timetables), Icons.AutoMirrored.Filled.EventNote),
        DashboardTile("students", stringResource(R.string.tile_students), Icons.Default.People),
        DashboardTile("admissions", stringResource(R.string.tile_new_admission), Icons.Default.School),
        DashboardTile("leave", stringResource(R.string.tile_manage_leaves), Icons.Default.HistoryEdu),
        DashboardTile("updates", stringResource(R.string.tile_updates), Icons.Default.Update),
        DashboardTile("settings", stringResource(R.string.settings), Icons.Default.Settings),
        DashboardTile("reports", stringResource(R.string.nav_reports), Icons.Default.Assessment),
        DashboardTile("quiz_upload", stringResource(R.string.tile_upload_quiz), Icons.Default.Quiz),
        DashboardTile("course_manager", stringResource(R.string.tile_course_manager), Icons.Default.VideoLibrary)
    ).filter { it.label.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                icon = Icons.Default.AutoGraph,
                title = stringResource(R.string.app_name),
                onNotificationClick = onNotificationClick,
                notificationCount = if (hasUnread) 1 else 0,
                onProfileClick = { onTileClick("settings") }
            )
        },
        bottomBar = {
            DashboardBottomBar(
                currentRoute = "dashboard",
                onNavigate = onNavigate
            )
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
            // Welcome Header with Date
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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
                                text = "Hello, $decodedName",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$decodedRole • ${getCurrentDate()}",
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
                                imageVector = Icons.Filled.School,
                                contentDescription = "Avatar",
                                modifier = Modifier.padding(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick Stats Row
            items(stats) { stat ->
                Box(modifier = Modifier.padding(horizontal = if (stats.indexOf(stat) % 2 == 0) 12.dp else 0.dp, vertical = 0.dp)) {
                    // Small adjustment to padding to match the previous separate grid
                    StatCard(stat = stat)
                }
            }

            // Motivational Banner
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
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
                                    text = stringResource(R.string.manage_your_school),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.everything_in_one_place),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            // Section Title + Filter
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { /* Optional: extra filter action */ }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                    }
                }
            }

            // Enhanced Search Bar
            item(span = { GridItemSpan(2) }) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }

            // Modern Tile Grid
            items(adminTiles, key = { it.id }) { tile ->
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    AnimatedDashboardTile(
                        tile = tile,
                        onClick = { onTileClick(tile.id) }
                    )
                }
            }

            // Decorative Footer
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.made_for_futures),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarEnhanced(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(30.dp)),
        placeholder = { Text("Search modules...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        shape = RoundedCornerShape(30.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        singleLine = true
    )
}

private fun getCurrentDate(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
    return LocalDate.now().format(formatter)
}

// Previews
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AdminDashboardDarkPreview() {
    MarkMyDayTheme {
        AdminDashboard(
            onNotificationClick = {},
            onTileClick = {},
            onNavigate = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    MarkMyDayTheme {
        AdminDashboard(
            onNotificationClick = {},
            onTileClick = {},
            onNavigate = {}
        )
    }
}