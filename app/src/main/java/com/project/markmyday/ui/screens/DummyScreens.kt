package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.ui.components.DashboardBottomBar
import com.project.markmyday.ui.components.DashboardTopBar

@Composable
fun HappeningsScreen(onNotificationClick: () -> Unit, onNavigate: (String) -> Unit) {
    DummyBaseScreen(
        title = "Notices & Events",
        icon = Icons.Default.Campaign,
        currentRoute = "happenings",
        onNotificationClick = onNotificationClick,
        onNavigate = onNavigate
    ) {
        Text("Latest School Updates 📢", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No new notices for today.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun LearningScreen(onNotificationClick: () -> Unit, onNavigate: (String) -> Unit) {
    DummyBaseScreen(
        title = "Learning Center",
        icon = Icons.Default.School,
        currentRoute = "SKP",
        onNotificationClick = onNotificationClick,
        onNavigate = onNavigate
    ) {
        Text("My Courses 📚", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your study material will appear here.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun ReportsScreen(onNotificationClick: () -> Unit, onNavigate: (String) -> Unit) {
    DummyBaseScreen(
        title = "Performance Reports",
        icon = Icons.Default.Assessment,
        currentRoute = "marks",
        onNotificationClick = onNotificationClick,
        onNavigate = onNavigate
    ) {
        Text("Academic Progress 📊", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Report cards will be available after exams.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun DummyBaseScreen(
    title: String,
    icon: ImageVector,
    currentRoute: String,
    onNotificationClick: () -> Unit,
    onNavigate: (String) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            DashboardTopBar(
                title = title,
                onNotificationClick = onNotificationClick,
                icon = icon
            )
        },
        bottomBar = {
            DashboardBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}
