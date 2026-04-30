package com.example.markmyday2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.markmyday2.ui.viewmodel.AdminViewModel
import com.example.markmyday2.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    adminViewModel: AdminViewModel = viewModel(),
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val users by adminViewModel.users.collectAsState()
    val classes by adminViewModel.classes.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val errorMessage by adminViewModel.errorMessage.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddClassDialog by remember { mutableStateOf(false) }
    var showAddTimetableDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { adminViewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { adminViewModel.seedClasses() }) {
                        Icon(Icons.Default.Add, contentDescription = "Seed Classes", tint = MaterialTheme.colorScheme.secondary)
                    }
                    TextButton(onClick = { 
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Text("Logout", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { showAddTimetableDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Timetable")
                }
                Spacer(modifier = Modifier.height(8.dp))
                SmallFloatingActionButton(
                    onClick = { showAddClassDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Class")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(onClick = { showAddUserDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add User")
                }
            }
        }
    ) { padding ->
        // ... (rest of the Scaffold content)
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
                    Text("Manage Classes", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(classes) { classInfo ->
                    AdminClassItem(classInfo = classInfo, onDelete = { adminViewModel.deleteClass(classInfo.classId) })
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Manage Users", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(users) { user ->
                    UserItem(user = user, onDelete = { adminViewModel.deleteUser(user.userId) })
                }
            }
        }

        if (showAddUserDialog) {
            AddUserDialog(
                classes = classes,
                onDismiss = { showAddUserDialog = false },
                onConfirm = { name, email, role, classId ->
                    adminViewModel.createUser(name, email, role, classId)
                    showAddUserDialog = false
                }
            )
        }

        if (showAddClassDialog) {
            AddClassDialog(
                teachers = users.filter { it.role == com.example.markmyday2.data.model.UserRole.TEACHER },
                onDismiss = { showAddClassDialog = false },
                onConfirm = { className, teacherId ->
                    adminViewModel.createClass(className, teacherId)
                    showAddClassDialog = false
                }
            )
        }

        if (showAddTimetableDialog) {
            AddTimetableDialog(
                classes = classes,
                teachers = users.filter { it.role == com.example.markmyday2.data.model.UserRole.TEACHER },
                onDismiss = { showAddTimetableDialog = false },
                onConfirm = { entry ->
                    adminViewModel.addTimetableEntry(entry)
                    showAddTimetableDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTimetableDialog(
    classes: List<com.example.markmyday2.data.model.ClassInfo>,
    teachers: List<com.example.markmyday2.data.model.User>,
    onDismiss: () -> Unit,
    onConfirm: (com.example.markmyday2.data.model.TimetableEntry) -> Unit
) {
    var selectedClassId by remember { mutableStateOf("") }
    var selectedTeacherName by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("Monday") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    
    var classExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Timetable Entry") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = classExpanded, onExpandedChange = { classExpanded = !classExpanded }) {
                    OutlinedTextField(
                        value = classes.find { it.classId == selectedClassId }?.className ?: "Select Class",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                        classes.forEach { classInfo ->
                            DropdownMenuItem(text = { Text(classInfo.className) }, onClick = {
                                selectedClassId = classInfo.classId
                                classExpanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = !dayExpanded }) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                        days.forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = {
                                day = d
                                dayExpanded = false
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = teacherExpanded, onExpandedChange = { teacherExpanded = !teacherExpanded }) {
                    OutlinedTextField(
                        value = selectedTeacherName.ifEmpty { "Select Teacher" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Teacher") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = teacherExpanded, onDismissRequest = { teacherExpanded = false }) {
                        if (teachers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No teachers found.") },
                                onClick = { teacherExpanded = false },
                                enabled = false
                            )
                        } else {
                            teachers.forEach { teacher ->
                                DropdownMenuItem(text = { Text(teacher.name) }, onClick = {
                                    selectedTeacherName = teacher.name
                                    teacherExpanded = false
                                })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start (e.g. 09:00)") }, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End (e.g. 10:00)") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(com.example.markmyday2.data.model.TimetableEntry(day, startTime, endTime, subject, selectedTeacherName, selectedClassId))
                },
                enabled = selectedClassId.isNotBlank() && subject.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassDialog(
    teachers: List<com.example.markmyday2.data.model.User>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var teacherId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Class") },
        text = {
            Column {
                OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class Name") })
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = teachers.find { it.userId == teacherId }?.name ?: "Select Teacher",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Teacher") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (teachers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No teachers found. Add a teacher first.") },
                                onClick = { expanded = false },
                                enabled = false
                            )
                        } else {
                            teachers.forEach { teacher ->
                                DropdownMenuItem(
                                    text = { Text(teacher.name) },
                                    onClick = {
                                        teacherId = teacher.userId
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(className, teacherId) },
                enabled = className.isNotBlank() && teacherId.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    classes: List<com.example.markmyday2.data.model.ClassInfo>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, com.example.markmyday2.data.model.UserRole, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(com.example.markmyday2.data.model.UserRole.STUDENT) }
    var classId by remember { mutableStateOf<String?>(null) }
    var roleExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        com.example.markmyday2.data.model.UserRole.values().forEach { userRole ->
                            DropdownMenuItem(
                                text = { Text(userRole.name) },
                                onClick = {
                                    role = userRole
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                if (role == com.example.markmyday2.data.model.UserRole.STUDENT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = !classExpanded }
                    ) {
                        OutlinedTextField(
                            value = classes.find { it.classId == classId }?.className ?: "Select Class",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }
                        ) {
                            classes.forEach { classInfo ->
                                DropdownMenuItem(
                                    text = { Text(classInfo.className) },
                                    onClick = {
                                        classId = classInfo.classId
                                        classExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, role, classId) },
                enabled = name.isNotBlank() && email.isNotBlank() && (role != com.example.markmyday2.data.model.UserRole.STUDENT || classId != null)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminClassItem(classInfo: com.example.markmyday2.data.model.ClassInfo, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(text = classInfo.className, style = MaterialTheme.typography.bodyLarge)
                Text(text = "ID: ${classInfo.classId}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun UserItem(user: com.example.markmyday2.data.model.User, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(text = user.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "${user.role} | ${user.email}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}