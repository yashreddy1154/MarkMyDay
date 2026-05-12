package com.project.markmyday.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.sp
import com.project.markmyday.R
import com.project.markmyday.data.model.Student
import com.project.markmyday.ui.components.DashboardTopBar
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.StudentRegistrationState

/**
 * Screen for managing students, organized by class.
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

    val studentsByClass = remember(filteredStudents) {
        filteredStudents.groupBy { it.studentClass }
            .toSortedMap(compareBy { it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0 })
    }

    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    if (studentToDelete != null) {
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text(stringResource(R.string.delete_confirmation, studentToDelete?.name ?: "")) },
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardTopBar(
                title = stringResource(R.string.students_list_title),
                onNotificationClick = { /* TODO */ },
                icon = Icons.Outlined.People,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            com.project.markmyday.ui.components.SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                studentsByClass.forEach { (className, studentsInClass) ->
                    item(key = className) {
                        ClassGroupCard(
                            className = className,
                            students = studentsInClass,
                            onEdit = { studentToEdit = it },
                            onDelete = { studentToDelete = it }
                        )
                    }
                }
                
                if (studentsByClass.isEmpty()) {
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
}

@Composable
fun ClassGroupCard(
    className: String,
    students: List<Student>,
    onEdit: (Student) -> Unit,
    onDelete: (Student) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayClassName = if (className.contains("Class")) className else "Class $className"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = className.filter { it.isDigit() }.ifEmpty { "?" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = displayClassName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${students.size} Students",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    students.forEach { student ->
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        StudentDetailItem(
                            student = student,
                            onEdit = { onEdit(student) },
                            onDelete = { onDelete(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDetailItem(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val parentName = if (student.motherName.isNotBlank()) student.motherName else student.fatherName
    val phone = if (student.motherPhone.isNotBlank()) student.motherPhone else student.fatherPhone

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: ${student.studentId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(parentName, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(phone, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
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
