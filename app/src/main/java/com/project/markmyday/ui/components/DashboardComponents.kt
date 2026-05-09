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
import androidx.compose.ui.res.stringResource
import com.project.markmyday.R
import com.project.markmyday.ui.theme.BadgeRed
import com.project.markmyday.ui.models.DashboardTile
import com.project.markmyday.ui.models.TimetableEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    title: String,
    onNotificationClick: () -> Unit,
    notificationCount: Int = 0,
    icon: ImageVector? = null
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    title, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(44.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        ), 
                        CircleShape
                    )
                    .clickable { onNotificationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications, 
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(BadgeRed, CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun WelcomeSection(
    name: String,
    role: String,
    homeSection: String? = null,
    subject: String? = null
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(id = R.string.hello), 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.WavingHand, 
                contentDescription = null, 
                tint = Color(0xFFFFD600),
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            name, 
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    role, 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (homeSection != null || subject != null) {
                    Text(
                        " • ",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${homeSection ?: ""} ${if (subject != null) "($subject)" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
            Text(stringResource(id = R.string.school_schedule), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* TODO */ }) {
                Text(stringResource(id = R.string.view_all), color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(entries) { entry ->
                TimetableCard(entry)
            }
        }
    }
}

@Composable
fun TimetableCard(entry: TimetableEntry) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(entry.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(entry.code, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Room, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(entry.room, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(entry.time, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DashboardTileGrid(tiles: List<DashboardTile>, onTileClick: (DashboardTile) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        tiles.chunked(2).forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowTiles.forEach { tile ->
                    Box(modifier = Modifier.weight(1f)) {
                        DashboardTileCard(tile, onTileClick)
                    }
                }
                // If the last row has only 1 item, add a spacer to maintain alignment
                if (rowTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DashboardTileCard(tile: DashboardTile, onClick: (DashboardTile) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick(tile) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle background accent
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
                                )
                            ), 
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tile.icon,
                        contentDescription = tile.label,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tile.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        lineHeight = 18.sp
                    )
                }

                if (tile.badgeCount != null || tile.badgeText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tile.badgeText ?: tile.badgeCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
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
        placeholder = { Text(stringResource(id = R.string.search_anything), color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun DashboardBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        val items = listOf(
            Triple("dashboard", stringResource(id = R.string.nav_home), Icons.Default.Home),
            Triple("happenings", stringResource(id = R.string.nav_notices), Icons.Default.Campaign),
            Triple("SKP", stringResource(id = R.string.nav_learning), Icons.Default.School),
            Triple("marks", stringResource(id = R.string.nav_reports), Icons.Default.Assessment)
        )

        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontWeight = FontWeight.Medium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
