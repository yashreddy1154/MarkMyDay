package com.project.markmyday.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.Question
import com.project.markmyday.viewmodel.QuizState
import com.project.markmyday.viewmodel.QuizTakingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingScreen(
    subject: String,
    className: String,
    userName: String = "Student",
    studentId: String = "",
    onBack: () -> Unit
) {
    val viewModel: QuizTakingViewModel = viewModel()
    val questions by viewModel.questions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val currentAttempt by viewModel.currentAttempt.collectAsState()
    val timeLeftFormatted by viewModel.timeLeftFormatted.collectAsState()
    val quizState by viewModel.quizState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe Lifecycle for anti-cheat
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (quizState is QuizState.InProgress && questions.isNotEmpty()) {
                    viewModel.onAppPaused()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startQuiz(className, userName, studentId, subject)
    }

    var showWarning by remember { mutableStateOf(false) }
    var warningMessage by remember { mutableStateOf("") }

    LaunchedEffect(quizState) {
        if (quizState is QuizState.Warning) {
            warningMessage = (quizState as QuizState.Warning).message
            showWarning = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(subject, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Class $className", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (quizState is QuizState.InProgress && questions.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = timeLeftFormatted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F7FA))) {
            when (quizState) {
                is QuizState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is QuizState.Error -> {
                    QuizErrorMessage((quizState as QuizState.Error).message, onBack)
                }
                is QuizState.InProgress, is QuizState.Warning -> {
                    if (questions.isEmpty()) {
                        QuizErrorMessage("No quiz available for this class right now.", onBack)
                    } else {
                        QuizContent(
                            questions = questions,
                            currentIndex = currentQuestionIndex,
                            selectedAnswers = currentAttempt.answers,
                            timeLeftFormatted = timeLeftFormatted,
                            onAnswerSelected = { qId, ans -> viewModel.onAnswerSelected(qId, ans) },
                            onNext = { viewModel.nextQuestion() },
                            onBack = { 
                                if (currentQuestionIndex > 0) {
                                    viewModel.previousQuestion()
                                } else {
                                    onBack()
                                }
                            }
                        )
                    }
                }
                is QuizState.Finished -> {
                    val finishedState = quizState as QuizState.Finished
                    ResultContent(
                        score = finishedState.score,
                        total = finishedState.total,
                        onBack = onBack
                    )
                }
                else -> {}
            }
        }

        if (showWarning) {
            AlertDialog(
                onDismissRequest = { showWarning = false },
                title = { Text("Warning") },
                text = { Text(warningMessage) },
                confirmButton = {
                    Button(onClick = { showWarning = false }) {
                        Text("I Understand")
                    }
                }
            )
        }
    }
}

@Composable
fun QuizErrorMessage(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📢",
                    fontSize = 48.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
fun QuizContent(
    questions: List<Question>,
    currentIndex: Int,
    selectedAnswers: Map<String, String>,
    timeLeftFormatted: String,
    onAnswerSelected: (String, String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val currentQuestion = questions.getOrNull(currentIndex) ?: return
    val selectedAnswer = selectedAnswers[currentQuestion.id]

    val purpleTheme = Color(0xFF917BFF) // Purple from screenshot
    val lightPurple = Color(0xFFF1EFFF)
    val yellowSelected = Color(0xFFFFD154)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(purpleTheme)
    ) {
        // Header with Back, Title, Timer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = currentQuestion.subject,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(timeLeftFormatted, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Progress Bar
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF8CFF70), // Light green progress
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Question ${currentIndex + 1} of ${questions.size}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(32.dp))

        // Question Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Subject Tag
                Surface(
                    color = purpleTheme.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = currentQuestion.subject,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = purpleTheme,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Question Text in its own card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text(
                        text = currentQuestion.text,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF1A202C),
                        lineHeight = 30.sp
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Options List
                val labels = listOf("A", "B", "C", "D")
                currentQuestion.options.forEachIndexed { index, option ->
                    val label = labels.getOrNull(index) ?: ""
                    QuizOptionItem(
                        label = label,
                        text = option,
                        isSelected = option == selectedAnswer,
                        onClick = { onAnswerSelected(currentQuestion.id, option) }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // Bottom Action Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedAnswer != null) purpleTheme else Color.LightGray
                )
            ) {
                Text(
                    text = if (currentIndex == questions.size - 1) "Finish" else "Check",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun QuizOptionItem(
    label: String,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val purpleTheme = Color(0xFF917BFF)
    val yellowSelected = Color(0xFFFFD154)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) yellowSelected else Color(0xFFE2E8F0)),
        color = if (isSelected) yellowSelected.copy(alpha = 0.1f) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) yellowSelected else Color(0xFFF7FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF2D3748) else Color(0xFF4A5568)
            )
        }
    }
}

@Composable
fun ResultContent(score: Int, total: Int, onBack: () -> Unit) {
    val purpleTheme = Color(0xFF917BFF)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(purpleTheme),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            border = BorderStroke(8.dp, Color(0xFFFFD154))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFFFD154)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Test Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your Score",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Text(
            text = "$score / $total",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text(
                "Back to Dashboard",
                color = purpleTheme,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
