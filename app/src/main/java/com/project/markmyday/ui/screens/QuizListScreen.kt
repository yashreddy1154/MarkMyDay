package com.project.markmyday.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.QuizTakingViewModel
import com.project.markmyday.viewmodel.QuizState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListScreen(
    className: String,
    onQuizSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: QuizTakingViewModel = viewModel()
    val availableQuizzes by viewModel.availableQuizzes.collectAsState()
    val quizState by viewModel.quizState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAvailableQuizzes(className)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Tests", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            if (quizState is QuizState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (availableQuizzes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📢", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        "No tests are available for your class yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val globalQuizzes = availableQuizzes.filter { it.second == "ALL" }
                    val classQuizzes = availableQuizzes.filter { it.second != "ALL" }

                    if (globalQuizzes.isNotEmpty()) {
                        item {
                            Text(
                                "School Global Tests 🌏",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(globalQuizzes) { quiz ->
                            QuizListItem(
                                title = quiz.first,
                                subtitle = "Available for everyone",
                                icon = Icons.Default.School,
                                color = Color(0xFF917BFF),
                                onClick = { onQuizSelected(quiz.first) }
                            )
                        }
                    }

                    if (classQuizzes.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Class $className Special Tests 🎒",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(classQuizzes) { quiz ->
                            QuizListItem(
                                title = quiz.first,
                                subtitle = "Targeted for Class $className",
                                icon = Icons.Default.Star,
                                color = Color(0xFFFFD154),
                                onClick = { onQuizSelected(quiz.first) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizListItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = color
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2D3748)
                )
                Text(
                    subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.Quiz,
                contentDescription = null,
                tint = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}
