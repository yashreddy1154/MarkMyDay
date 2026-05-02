package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.viewmodel.AssignmentSubmissionState
import com.project.markmyday.viewmodel.AssignmentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentScreen(
    onBack: () -> Unit,
    viewModel: AssignmentViewModel = viewModel()
) {
    val assignedClasses by viewModel.assignedClasses.collectAsState()
    val submissionState by viewModel.submissionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedClass by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Assignment") }
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    LaunchedEffect(submissionState) {
        if (submissionState is AssignmentSubmissionState.Success) {
            snackbarHostState.showSnackbar("Assignment posted successfully!")
            title = ""
            viewModel.resetSubmissionState()
        } else if (submissionState is AssignmentSubmissionState.Error) {
            snackbarHostState.showSnackbar((submissionState as AssignmentSubmissionState.Error).message)
            viewModel.resetSubmissionState()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Post Assignment/Exam", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Class Selection
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedClass,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Class") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    assignedClasses.forEach { className ->
                        DropdownMenuItem(
                            text = { Text(className) },
                            onClick = {
                                selectedClass = className
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Type Selection
            Column {
                Text("Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "Assignment", onClick = { type = "Assignment" })
                    Text("Assignment", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = type == "Exam", onClick = { type = "Exam" })
                    Text("Exam")
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (e.g., Algebra Test)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Date Picker
            OutlinedTextField(
                value = dateFormatter.format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text(if (type == "Assignment") "Due Date" else "Exam Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Post Button
            Button(
                onClick = {
                    viewModel.postAssignment(type, title, selectedClass, Date(selectedDate))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = submissionState !is AssignmentSubmissionState.Loading
            ) {
                if (submissionState is AssignmentSubmissionState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Post $type", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
