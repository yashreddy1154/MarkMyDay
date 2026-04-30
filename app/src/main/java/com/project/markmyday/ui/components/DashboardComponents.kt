package com.project.markmyday.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry
import com.project.markmyday.ui.theme.BadgeRed
import com.project.markmyday.ui.theme.OrangeGradientEnd
import com.project.markmyday.ui.theme.OrangeGradientStart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    title: String,
    onNotificationClick: () -> Unit,
    notificationCount: Int = 0
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        actions = {
            Box(modifier = Modifier.padding(end = 16.dp).clickable { onNotificationClick() }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(BadgeRed, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun WelcomeSection(name: String, role: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome,", fontSize = 16.sp, color = Color.Gray)
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(role, fontSize = 14.sp, color = OrangeGradientStart, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TimetableSection(entries: List<TimetableEntry>) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Today's Timetable", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("View All", color = OrangeGradientStart, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(entries) { entry ->
                TimetableCard(entry)
            }
        }
    }
}

@Composable
fun TimetableCard(entry: TimetableEntry) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(OrangeGradientStart, OrangeGradientEnd)))
                    .padding(16.dp)
            ) {
                Column {
                    Text(entry.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(entry.code, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(entry.room, color = Color.White, fontSize = 14.sp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.time, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DashboardTileGrid(tiles: List<DashboardTile>, onTileClick: (DashboardTile) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp) // Fixed height or adjust as needed
        ) {
            items(tiles) { tile ->
                DashboardTileCard(tile, onTileClick)
            }
        }
    }
}

@Composable
fun DashboardTileCard(tile: DashboardTile, onClick: (DashboardTile) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick(tile) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            if (tile.badgeCount != null || tile.badgeText != null) {
                Box(
                    modifier = Modifier
                        .background(OrangeGradientStart.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = tile.badgeText ?: tile.badgeCount.toString(),
                        color = OrangeGradientStart,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = tile.label,
                    modifier = Modifier.size(32.dp),
                    tint = OrangeGradientStart
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tile.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        placeholder = { Text("Search records...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun DashboardBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "dashboard",
            onClick = { onNavigate("dashboard") },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeGradientStart,
                selectedTextColor = OrangeGradientStart,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = OrangeGradientStart.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "happenings",
            onClick = { onNavigate("happenings") },
            icon = { Icon(Icons.Default.Newspaper, contentDescription = null) },
            label = { Text("Happenings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeGradientStart,
                selectedTextColor = OrangeGradientStart,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = OrangeGradientStart.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "SKP",
            onClick = { onNavigate("SKP") },
            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
            label = { Text("RMS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeGradientStart,
                selectedTextColor = OrangeGradientStart,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = OrangeGradientStart.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "marks",
            onClick = { onNavigate("marks") },
            icon = { Icon(Icons.Default.List, contentDescription = null) },
            label = { Text("View Marks") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeGradientStart,
                selectedTextColor = OrangeGradientStart,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = OrangeGradientStart.copy(alpha = 0.1f)
            )
        )
    }
}
