package com.project.markmyday.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.markmyday.R
import com.project.markmyday.data.model.Student
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.StudentRegistrationState

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
            students
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
            title = { Text(stringResource(R.string.delete_confirmation, studentToDelete?.name ?: "")) },
            text = { Text(stringResource(R.string.delete_confirmation, studentToDelete?.name ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        studentToDelete?.let { onDeleteStudent(it) }
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text(stringResource(R.string.cancel))
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
                    gender = studentToEdit?.gender ?: "",
                    motherName = studentToEdit?.motherName ?: "",
                    motherPhone = studentToEdit?.motherPhone ?: "",
                    fatherName = studentToEdit?.fatherName ?: "",
                    fatherPhone = studentToEdit?.fatherPhone ?: "",
                    studentClass = studentToEdit?.studentClass ?: "",
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
                title = stringResource(R.string.edit_staff_details),
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
                                gender = editState.gender,
                                motherName = editState.motherName,
                                motherPhone = editState.motherPhone,
                                fatherName = editState.fatherName,
                                fatherPhone = editState.fatherPhone,
                                studentClass = editState.studentClass,
                                email = editState.email
                            )
                        )
                    }
                    studentToEdit = null
                },
                registrationState = StudentRegistrationState.Idle
            )
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.students_list_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = { },
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) { }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredStudents, key = { it.studentId }) { student ->
                StudentListItem(
                    student = student,
                    onEdit = { studentToEdit = student },
                    onDelete = { studentToDelete = student }
                )
            }
            
            if (filteredStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_students_found, searchQuery),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
    val initials = student.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
    val parentName = if (student.motherName.isNotBlank()) student.motherName else student.fatherName
    val phone = if (student.motherPhone.isNotBlank()) student.motherPhone else student.fatherPhone

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Header: Avatar, Name, and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Avatar with Initials
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.class_format, student.studentClass.toIntOrNull() ?: 0),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info Section: ID and Category Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoTag(
                    icon = Icons.Default.Badge,
                    text = student.studentId,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                val color = when (student.category.lowercase()) {
                    "primary" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                    "secondary" -> Color(0xFF2196F3).copy(alpha = 0.15f)
                    "high school" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                }
                val textColor = when (student.category.lowercase()) {
                    "primary" -> Color(0xFF2E7D32)
                    "secondary" -> Color(0xFF1565C0)
                    "high school" -> Color(0xFFEF6C00)
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }

                Surface(
                    color = color,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = student.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Contact and Personal Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.parent_label, parentName),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
        Student(studentId = "100001", name = "John Doe", studentClass = "10", category = "High School"),
        Student(studentId = "100002", name = "Jane Smith", studentClass = "5", category = "Primary"),
        Student(studentId = "100003", name = "Michael Brown", studentClass = "7", category = "Secondary")
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
