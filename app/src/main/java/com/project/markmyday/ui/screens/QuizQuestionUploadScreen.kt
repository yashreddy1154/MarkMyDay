package com.project.markmyday.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.data.model.Question
import com.project.markmyday.viewmodel.QuizViewModel
import com.project.markmyday.viewmodel.UploadStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizQuestionUploadScreen(
    onBack: () -> Unit
) {
    val viewModel: QuizViewModel = viewModel()
    val uploadStatus by viewModel.uploadStatus.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var subject by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    
    val options = listOf("Option A", "Option B", "Option C", "Option D")
    var selectedCorrectOption by remember { mutableStateOf(options[0]) }
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadCsv(context, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. CSV Upload Section (Large Box at Top)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clickable { launcher.launch("*/*") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Upload CSV File", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Class-specific questions from file", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Divider(color = Color.LightGray.copy(alpha = 0.5f))

            // 2. Manual Entry Section
            Text("Manual Entry", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Subject and Class row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject (e.g. GK)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class (e.g. 10)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Question Box
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Enter Question Text") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Options Grid (A, B, C, D)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                }

                // Correct Option Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCorrectOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Correct Option") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    selectedCorrectOption = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        if (questionText.isNotEmpty() && subject.isNotEmpty() && className.isNotEmpty()) {
                            val correctValue = when(selectedCorrectOption) {
                                "Option A" -> optA
                                "Option B" -> optB
                                "Option C" -> optC
                                else -> optD
                            }
                            val normalizedClass = if (!className.startsWith("Class")) "Class $className" else className
                            
                            viewModel.saveManualQuestion(
                                Question(
                                    text = questionText,
                                    options = listOf(optA, optB, optC, optD),
                                    correctAnswer = correctValue,
                                    subject = subject,
                                    className = normalizedClass
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uploadStatus !is UploadStatus.Loading
                ) {
                    Text("Add to Question Bank", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Status Indication
            when (uploadStatus) {
                is UploadStatus.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                is UploadStatus.Success -> {
                    Text("Saved successfully!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    // Reset fields on success
                    LaunchedEffect(Unit) {
                        questionText = ""; optA = ""; optB = ""; optC = ""; optD = ""
                        kotlinx.coroutines.delay(2000)
                        viewModel.resetStatus()
                    }
                }
                is UploadStatus.Error -> Text("Error: ${(uploadStatus as UploadStatus.Error).message}", color = Color.Red)
                else -> {}
            }
        }
    }
}
