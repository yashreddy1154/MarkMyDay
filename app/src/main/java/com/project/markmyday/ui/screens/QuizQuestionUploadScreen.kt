package com.project.markmyday.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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

    // Handle Toast messages
    LaunchedEffect(uploadStatus) {
        when (uploadStatus) {
            is UploadStatus.Success -> {
                Toast.makeText(context, "Saved successfully! ✨", Toast.LENGTH_SHORT).show()
                questionText = ""; optA = ""; optB = ""; optC = ""; optD = ""; subject = ""
                viewModel.resetStatus()
            }
            is UploadStatus.Error -> {
                Toast.makeText(context, "Error: ${(uploadStatus as UploadStatus.Error).message}", Toast.LENGTH_LONG).show()
                viewModel.resetStatus()
            }
            else -> {}
        }
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
                    Text("Upload CSV/Excel File", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Format: Subject | Class | Question | Options | Correct", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            // 2. Manual Entry Section
            Text("Manual Entry 📝", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("Note: Manual entries are added to ALL classes.", fontSize = 12.sp, color = Color.Gray)

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. GK)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Enter subject name") }
                )

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Enter Question Text") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Type the question here...") }
                )

                // Options Grid (A, B, C, D)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                        OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
                        OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp))
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
                        label = { Text("Select Correct Option") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
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
                        if (questionText.isNotEmpty() && subject.isNotEmpty()) {
                            val correctValue = when(selectedCorrectOption) {
                                "Option A" -> optA
                                "Option B" -> optB
                                "Option C" -> optC
                                else -> optD
                            }
                            
                            viewModel.saveManualQuestion(
                                Question(
                                    text = questionText,
                                    options = listOf(optA, optB, optC, optD),
                                    correctAnswer = correctValue,
                                    subject = subject,
                                    className = "ALL" // Requirement: manual always ALL
                                )
                            )
                        } else {
                            Toast.makeText(context, "Please fill Subject and Question", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    enabled = uploadStatus !is UploadStatus.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (uploadStatus is UploadStatus.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save to Question Bank ✨", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
