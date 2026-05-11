package com.project.markmyday.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.data.model.QuizResult
import com.project.markmyday.ui.components.DashboardBottomBar
import com.project.markmyday.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    role: String = "student",
    userClass: String = "10", // Should be passed from dashboard
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val viewModel: LeaderboardViewModel = viewModel()
    val filteredResults by viewModel.filteredResults.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val isAdmin = role.lowercase() == "admin" || role.lowercase() == "principal"
    val purpleTheme = Color(0xFF917BFF)

    LaunchedEffect(Unit) {
        viewModel.filterAndSort("Overall", userRole = role, userClass = userClass)
    }

    Scaffold(
        containerColor = purpleTheme,
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = { 
                            downloadLeaderboardCsv(context, filteredResults)
                        }) {
                            Icon(Icons.Default.Download, contentDescription = "Download CSV", tint = Color.White)
                        }
                        IconButton(onClick = { 
                            viewModel.clearLeaderboard()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            DashboardBottomBar(currentRoute = "marks", onNavigate = onNavigate)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top 3 Podium
            val top3 = filteredResults.take(3)
            if (top3.isNotEmpty()) {
                Top3Podium(top3)
            }

            Spacer(Modifier.height(24.dp))

            // Remaining List
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f, fill = false),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    filteredResults.drop(3).forEachIndexed { index, result ->
                        RemainingLeaderboardItem(rank = index + 4, result = result)
                        if (index < filteredResults.size - 4) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Top3Podium(top3: List<QuizResult>) {
    val first = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (first != null) {
            Text("Congrats", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Text(first.studentName, color = Color(0xFFFFD154), fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(24.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // 2nd Place
            PodiumItem(result = second, rank = 2, avatarSize = 80.dp, color = Color(0xFFFFB7B7))
            
            // 1st Place
            PodiumItem(result = first, rank = 1, avatarSize = 110.dp, color = Color(0xFFFFD154))
            
            // 3rd Place
            PodiumItem(result = third, rank = 3, avatarSize = 80.dp, color = Color(0xFFB7DFFF))
        }
    }
}

@Composable
fun PodiumItem(result: QuizResult?, rank: Int, avatarSize: androidx.compose.ui.unit.Dp, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (result != null) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(avatarSize),
                    shape = CircleShape,
                    border = BorderStroke(4.dp, color),
                    color = Color.White
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = Color.LightGray
                    )
                }
                // Crown or Rank badge could go here
            }
            Spacer(Modifier.height(8.dp))
            Text(result.studentName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (result.className.isNotEmpty()) {
                Text("Class ${result.className}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            Surface(
                color = if (rank == 1) Color(0xFFFFD154) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = if (rank == 1) Color.White else Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${result.score * 100} Point", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RemainingLeaderboardItem(rank: Int, result: QuizResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = Color(0xFFB7DFFF).copy(alpha = 0.2f)
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.padding(8.dp), tint = Color.LightGray)
        }
        
        Spacer(Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF917BFF)),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(result.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2D3748))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("@${result.studentId}", fontSize = 12.sp, color = Color.Gray)
                if (result.className.isNotEmpty()) {
                    Text(" • Class ${result.className}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFB7DFFF), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("${result.score * 100} Point", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D3748))
        }
    }
}

fun downloadLeaderboardCsv(context: android.content.Context, results: List<QuizResult>) {
    try {
        val fileName = "leaderboard_${System.currentTimeMillis()}.csv"
        val csvHeader = "Rank,Student Name,Student ID,Class,Subject,Score,Total Questions,Timestamp\n"
        val csvBody = results.mapIndexed { index, result ->
            "${index + 1},${result.studentName},${result.studentId},${result.className},${result.subject},${result.score},${result.totalQuestions},${result.timestamp}"
        }.joinToString("\n")
        
        val csvContent = csvHeader + csvBody
        
        val resolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
        }
        
        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }
            android.widget.Toast.makeText(context, "CSV downloaded to Downloads folder", android.widget.Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        android.util.Log.e("LeaderboardCSV", "Error exporting CSV", e)
        android.widget.Toast.makeText(context, "Error exporting CSV: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

