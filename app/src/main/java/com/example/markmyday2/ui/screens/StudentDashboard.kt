package com.example.markmyday2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markmyday2.data.model.AttendanceStatus
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.ui.viewmodel.StudentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    studentId: String,
    viewModel: StudentViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val records by viewModel.attendanceRecords.collectAsState()
    val percentage by viewModel.attendancePercentage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showTimetable by remember { mutableStateOf(false) }

    LaunchedEffect(studentId) {
        viewModel.loadAttendance(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Dashboard") },
                actions = {
                    TextButton(onClick = { showTimetable = true }) {
                        Text("Timetable")
                    }
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Overall Attendance", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = String.format("%.1f%%", percentage),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Attendance History", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(records.sortedByDescending { it.date }) { record ->
                        AttendanceRecordItem(record)
                    }
                }
            }
        }

        if (showTimetable) {
            val timetable by viewModel.timetable.collectAsState()
            
            // For demo, we need the student's classId. 
            // In a real app, this is part of the User model.
            // Since we don't have a specific 'classId' passed in yet, 
            // we'll assume the viewModel can find it or we fetch it.
            // For now, let's fetch for "demo_class" or similar if possible.
            
            AlertDialog(
                onDismissRequest = { showTimetable = false },
                title = { Text("Weekly Timetable") },
                text = {
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        if (timetable.isEmpty()) {
                            Text("No timetable entries found. (Make sure Admin added entries for your class)")
                        } else {
                            LazyColumn {
                                items(timetable.sortedBy { it.day }) { entry ->
                                    TimetableRow(entry)
                                }
                            }
                        }
                    }
                },
                confirmButton = { Button(onClick = { showTimetable = false }) { Text("Close") } }
            )
        }
    }
}

@Composable
fun AttendanceRecordItem(record: com.example.markmyday2.data.model.Attendance) {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = sdf.format(Date(record.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(dateString)
            Text(
                text = record.status.name,
                color = if (record.status == AttendanceStatus.PRESENT) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
