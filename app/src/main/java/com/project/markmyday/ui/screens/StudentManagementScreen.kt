package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.markmyday.data.model.Student
import com.project.markmyday.ui.theme.MarkMyDayTheme

/**
 * Screen for managing students, featuring a search bar and a list of students.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    students: List<Student>,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(searchQuery, students) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            students.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.studentId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    if (studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student") },
            text = { Text("Are you sure you want to delete ${studentToDelete?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        studentToDelete?.let { onDeleteStudent(it) }
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (studentToEdit != null) {
        var editState by remember { 
            mutableStateOf(
                AddStudentFormState(
                    name = studentToEdit?.name ?: "",
                    age = studentToEdit?.age?.toString() ?: "",
                    dob = studentToEdit?.dob ?: "",
                    parentName = studentToEdit?.parentName ?: "",
                    phone = studentToEdit?.phone ?: "",
                    studentClass = studentToEdit?.studentClass ?: "",
                    section = studentToEdit?.section ?: "",
                    email = studentToEdit?.email ?: ""
                )
            )
        }

        ModalBottomSheet(
            onDismissRequest = { studentToEdit = null },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            AddStudentContent(
                title = "Edit Student Details",
                state = editState,
                onStateChange = { editState = it },
                onBack = { studentToEdit = null },
                onSubmit = {
                    studentToEdit?.let { original ->
                        onEditStudent(
                            original.copy(
                                name = editState.name,
                                age = editState.age.toIntOrNull() ?: 0,
                                dob = editState.dob,
                                parentName = editState.parentName,
                                phone = editState.phone,
                                studentClass = editState.studentClass,
                                section = editState.section,
                                email = editState.email
                            )
                        )
                    }
                    studentToEdit = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text("Search by Name or ID") },
                    leadingIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { }
            }
        }
    ) { padding ->
        if (searchQuery.isNotBlank()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Student List (${filteredStudents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(filteredStudents, key = { it.studentId }) { student ->
                    StudentListItem(
                        student = student,
                        onEdit = { studentToEdit = student },
                        onDelete = { studentToDelete = student }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Search for students to view details",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * A list item representing a single student.
 */
@Composable
fun StudentListItem(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${student.studentId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = student.studentClass,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Sec: ${student.section}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentManagementScreenPreview() {
    val dummyStudents = listOf(
        Student(studentId = "S1001C10", name = "John Doe", studentClass = "Class 10", section = "A"),
        Student(studentId = "S1002C10", name = "Jane Smith", studentClass = "Class 10", section = "B"),
        Student(studentId = "S1003C9", name = "Michael Brown", studentClass = "Class 9", section = "A")
    )
    MarkMyDayTheme {
        StudentManagementScreen(
            students = dummyStudents,
            onEditStudent = {},
            onDeleteStudent = {},
            onBack = {}
        )
    }
}
