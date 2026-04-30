package com.example.markmyday2.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markmyday2.data.model.ClassInfo
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.ui.viewmodel.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboard(
    teacherId: String,
    viewModel: TeacherViewModel = viewModel(),
    onClassClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val classes by viewModel.classes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedClassForTimetable by remember { mutableStateOf<String?>(null) }
    val timetable = remember { mutableStateMapOf<String, List<TimetableEntry>>() }

    LaunchedEffect(teacherId) {
        viewModel.loadAssignedClasses(teacherId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Dashboard") },
                actions = {
                    Button(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                item {
                    Text("Your Classes", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(classes) { classInfo ->
                    ClassItem(
                        classInfo = classInfo, 
                        onClick = { onClassClick(classInfo.classId) },
                        onViewTimetable = { selectedClassForTimetable = classInfo.classId }
                    )
                }
            }
        }

        if (selectedClassForTimetable != null) {
            TimetableDialog(
                classId = selectedClassForTimetable!!,
                onDismiss = { selectedClassForTimetable = null }
            )
        }
    }
}

@Composable
fun ClassItem(classInfo: ClassInfo, onClick: () -> Unit, onViewTimetable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = classInfo.className, style = MaterialTheme.typography.bodyLarge)
                Text(text = "ID: ${classInfo.classId}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onViewTimetable) {
                Text("Timetable")
            }
        }
    }
}

@Composable
fun TimetableDialog(
    classId: String, 
    viewModel: TeacherViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val timetable by viewModel.timetable.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(classId) {
        viewModel.loadTimetable(classId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Class Timetable - $classId") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                } else if (timetable.isEmpty()) {
                    Text("No timetable entries found for this class.")
                } else {
                    LazyColumn {
                        items(timetable.sortedBy { it.day }) { entry ->
                            TimetableRow(entry)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun TimetableRow(entry: TimetableEntry) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(entry.day, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("${entry.startTime} - ${entry.endTime}", style = MaterialTheme.typography.labelSmall)
            }
            Text(entry.subject, style = MaterialTheme.typography.bodyLarge)
            Text("Teacher: ${entry.teacherName}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
