package com.project.markmyday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.NotificationViewModel
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
import androidx.compose.ui.res.stringResource
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile
import androidx.compose.ui.tooling.preview.Preview
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.R

@Composable
fun AdminDashboard(
    userName: String = "Principal Sharma",
    userRole: String = "Administrator",
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val notificationViewModel: NotificationViewModel = viewModel()
    val hasUnread by notificationViewModel.hasUnreadNotices.collectAsState()

    LaunchedEffect(userRole) {
        notificationViewModel.fetchNotifications(userRole)
    }

    var searchQuery by remember { mutableStateOf("") }

    val adminTiles = listOf(
        DashboardTile("notices", stringResource(R.string.tile_notices), Icons.Default.Campaign),
        DashboardTile("staff_management", stringResource(R.string.tile_staff_list), Icons.Default.Badge),
        DashboardTile("add_staff", stringResource(R.string.tile_add_staff), Icons.Default.PersonAdd),
        DashboardTile("add_student", stringResource(R.string.tile_add_student), Icons.Default.PersonAddAlt1),
        DashboardTile("timetable", stringResource(R.string.tile_timetables), Icons.AutoMirrored.Filled.EventNote),
        DashboardTile("students", stringResource(R.string.tile_students), Icons.Default.People),
        DashboardTile("admissions", stringResource(R.string.tile_new_admission), Icons.Default.School),
        DashboardTile("updates", stringResource(R.string.tile_updates), Icons.Default.Update),
        DashboardTile("settings", stringResource(R.string.settings), Icons.Default.Settings),
        DashboardTile("reports", stringResource(R.string.nav_reports), Icons.Default.Assessment)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                icon = Icons.Default.AutoGraph,
                title = stringResource(R.string.app_name),
                onNotificationClick = onNotificationClick,
                notificationCount = if (hasUnread) 1 else 0
            )
        },
        bottomBar = {
            DashboardBottomBar(
                currentRoute = "dashboard",
                onNavigate = onNavigate
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section with App Icon and Welcome
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
                        role = userRole
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "App Icon",
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // School Theme: "Learning is Fun" Banner
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
                            stringResource(R.string.manage_your_school),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.everything_in_one_place),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        }
                        Icon(
                            Icons.Default.Celebration,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.dashboard_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            DashboardTileGrid(
                tiles = adminTiles.filter { it.label.contains(searchQuery, ignoreCase = true) },
                onTileClick = { onTileClick(it.id) }
            )
            
            // Decorative footer for children theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.made_for_futures),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
