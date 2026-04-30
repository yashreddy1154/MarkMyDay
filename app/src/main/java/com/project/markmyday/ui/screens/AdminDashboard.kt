package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.markmyday.ui.components.*
import com.project.markmyday.ui.models.DashboardTile

import androidx.compose.ui.tooling.preview.Preview
import com.project.markmyday.ui.theme.MarkMyDayTheme

@Composable
fun AdminDashboard(
    onNotificationClick: () -> Unit,
    onTileClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val adminTiles = listOf(
        DashboardTile("notices", "Send Notices", Icons.Default.Campaign),
        DashboardTile("add_staff", "Add Staff", Icons.Default.PersonAdd),
        DashboardTile("timetable", "Timetable CRUD", Icons.Default.EventNote),
        DashboardTile("students", "Students CRUD", Icons.Default.People),
        DashboardTile("admissions", "Admissions", Icons.Default.School),
        DashboardTile("updates", "Global Updates", Icons.Default.Update),
        DashboardTile("reports", "Reports", Icons.Default.Assessment),
        DashboardTile("settings", "Settings", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            DashboardTopBar(
                title = "Admin Dashboard",
                onNotificationClick = onNotificationClick,
                notificationCount = 5
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
            WelcomeSection(name = "Principal Sharma", role = "Administrator")
            
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DashboardTileGrid(tiles = adminTiles, onTileClick = { onTileClick(it.id) })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardPreview() {
    MarkMyDayTheme {
        AdminDashboard(onNotificationClick = {}, onTileClick = {})
    }
}
