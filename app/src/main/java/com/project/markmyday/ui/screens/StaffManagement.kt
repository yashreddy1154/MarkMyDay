package com.project.markmyday.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.ui.theme.MarkMyDayTheme
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    teachers: List<Teacher>,
    onEditTeacher: (Teacher) -> Unit,
    onDeleteTeacher: (Teacher) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTeachers = remember(searchQuery, teachers) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            teachers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.teacherId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    var teacherToEdit by remember { mutableStateOf<Teacher?>(null) }
    var teacherToDelete by remember { mutableStateOf<Teacher?>(null) }

    if (teacherToDelete != null) {
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            title = { Text("Delete Staff") },
            text = { Text("Are you sure you want to delete ${teacherToDelete?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        teacherToDelete?.let { onDeleteTeacher(it) }
                        teacherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { teacherToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (teacherToEdit != null) {
        var editState by remember { 
            mutableStateOf(
                AddStaffFormState(
                    name = teacherToEdit?.name ?: "",
                    age = teacherToEdit?.age?.toString() ?: "",
                    dob = teacherToEdit?.dob ?: "",
                    phone = teacherToEdit?.phone ?: "",
                    email = teacherToEdit?.email ?: "",
                    subject = teacherToEdit?.subject ?: "",
                    homeSection = teacherToEdit?.homeSection ?: "",
                    classesTaught = teacherToEdit?.classesTaught ?: emptyList()
                )
            )
        }

        ModalBottomSheet(
            onDismissRequest = { teacherToEdit = null },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            AddStaffContent(
                title = "Edit Staff Details",
                state = editState,
                onStateChange = { editState = it },
                onBack = { teacherToEdit = null },
                onSubmit = {
                    teacherToEdit?.let { original ->
                        onEditTeacher(
                            original.copy(
                                name = editState.name,
                                age = editState.age.toIntOrNull() ?: 0,
                                dob = editState.dob,
                                phone = editState.phone,
                                subject = editState.subject,
                                homeSection = editState.homeSection,
                                classesTaught = editState.classesTaught
                            )
                        )
                    }
                    teacherToEdit = null
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
                        text = "Staff List (${filteredTeachers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(filteredTeachers, key = { it.teacherId }) { teacher ->
                    TeacherListItem(
                        teacher = teacher,
                        onEdit = { teacherToEdit = teacher },
                        onDelete = { teacherToDelete = teacher }
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
                    text = "Search for staff members to view details",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TeacherListItem(
    teacher: Teacher,
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
                    text = teacher.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${teacher.teacherId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = teacher.subject,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
fun StaffManagementScreenPreview() {
    val dummyTeachers = listOf(
        Teacher(teacherId = "T1261MAT", name = "Rahul Sharma", subject = "Maths"),
        Teacher(teacherId = "T1261SCI", name = "Priya Varma", subject = "Science"),
        Teacher(teacherId = "T1261ENG", name = "Amit Singh", subject = "English")
    )
    MarkMyDayTheme {
        StaffManagementScreen(
            teachers = dummyTeachers,
            onEditTeacher = {},
            onDeleteTeacher = {},
            onBack = {}
        )
    }
}
