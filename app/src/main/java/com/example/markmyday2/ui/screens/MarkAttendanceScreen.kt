package com.example.markmyday2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markmyday2.data.model.AttendanceStatus
import com.example.markmyday2.ui.viewmodel.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    classId: String,
    teacherId: String,
    viewModel: TeacherViewModel = viewModel(),
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val submissionStatus by viewModel.submissionStatus.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Maps studentId to their attendance status
    val attendanceMap = remember { mutableStateMapOf<String, AttendanceStatus>() }

    LaunchedEffect(classId) {
        viewModel.loadStudents(classId)
    }

    LaunchedEffect(submissionStatus) {
        submissionStatus?.onSuccess {
            snackbarHostState.showSnackbar("Attendance submitted successfully!")
            onBack()
        }?.onFailure {
            snackbarHostState.showSnackbar("Error: ${it.message}")
            viewModel.resetSubmissionStatus()
        }
    }
    
    // Initialize map when students are loaded
    LaunchedEffect(students) {
        students.forEach { student ->
            if (!attendanceMap.containsKey(student.userId)) {
                attendanceMap[student.userId] = AttendanceStatus.ABSENT
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mark Attendance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { 
                    viewModel.submitAttendance(classId, teacherId, attendanceMap.toMap())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = students.isNotEmpty() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Attendance")
                }
            }
        }
    ) { padding ->
        if (students.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No students found in this class")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(students) { student ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name, style = MaterialTheme.typography.bodyLarge)
                            Text(student.email, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Absent", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = attendanceMap[student.userId] == AttendanceStatus.PRESENT,
                                onCheckedChange = { isPresent ->
                                    attendanceMap[student.userId] = if (isPresent) AttendanceStatus.PRESENT else AttendanceStatus.ABSENT
                                },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text("Present", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
