package com.project.markmyday.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.project.markmyday.viewmodel.TeacherRegistrationState
import com.project.markmyday.viewmodel.TeacherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.markmyday.ui.theme.MarkMyDayTheme
import androidx.compose.ui.tooling.preview.Preview
import com.project.markmyday.R
import java.util.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Period

// Helper functions for date & age
fun calculateAgeFromDateOfBirth(dateOfBirth: Date?): Int? {
    if (dateOfBirth == null) return null
    val today = Calendar.getInstance()
    val dobCal = Calendar.getInstance().apply { time = dateOfBirth }
    var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}

fun formatDateForDisplay(date: Date?): String {
    if (date == null) return ""
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(date.toInstant())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffScreen(
    onBack: () -> Unit,
    onSubmit: (AddStaffFormState) -> Unit,
    viewModel: TeacherViewModel = viewModel()
) {
    var state by remember { mutableStateOf(AddStaffFormState()) }
    val registrationState by viewModel.registrationState.collectAsState()
    val context = LocalContext.current

    // Show success Toast but do NOT navigate back
    LaunchedEffect(registrationState) {
        if (registrationState is TeacherRegistrationState.Success) {
            Toast.makeText(context, "Staff member added successfully!", Toast.LENGTH_LONG).show()
            // Optional: reset form to allow adding another staff without leaving the screen
            // state = AddStaffFormState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddStaffContent(
            state = state,
            onStateChange = { state = it },
            onBack = onBack,
            onSubmit = { onSubmit(state) }
        )

        // Loading overlay
        if (registrationState is TeacherRegistrationState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Error dialog
        if (registrationState is TeacherRegistrationState.Error) {
            val errorMessage = (registrationState as TeacherRegistrationState.Error).message
            AlertDialog(
                onDismissRequest = { /* Keep showing until fixed */ },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { /* Optionally reset error state in ViewModel */ }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

data class AddStaffFormState(
    val name: String = "",
    val age: String = "",
    val dob: String = "",
    val phone: String = "",
    val email: String = "",
    val subject: String = "",
    val homeSection: String = "",
    val classesTaught: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddStaffContent(
    state: AddStaffFormState,
    onStateChange: (AddStaffFormState) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val subjects = listOf("Telugu", "Hindi", "English", "Maths", "Science", "Physics", "Biology", "Computers", "Social", "Games/PT")
    val sections = listOf("1A", "1B", "2A", "2B", "3A", "3B", "4A", "4B", "5A", "5B", "6A", "6B", "7A", "7B", "8A", "8B", "9A", "9B", "10A", "10B")
    val classes = (1..10).map { "Class $it" }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Automatically show date picker when DOB field is clicked
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            val age = calculateAgeFromDateOfBirth(selectedDate)
                            if (age != null && age <= 100) {
                                val formattedDate = formatDateForDisplay(selectedDate)
                                onStateChange(
                                    state.copy(
                                        dob = formattedDate,
                                        age = age.toString()
                                    )
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Age must be 100 years or less. Please select a valid date.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        showDatePicker = false
                    }
                ) {
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

    // Form completion progress (7 mandatory fields)
    val requiredFields = listOf(
        state.name.isNotBlank(),
        state.age.isNotBlank(),
        state.phone.isNotBlank(),
        state.email.isNotBlank(),
        state.subject.isNotBlank(),
        state.homeSection.isNotBlank(),
        state.classesTaught.isNotEmpty()
    )
    val filledCount = requiredFields.count { it }
    val progress = if (requiredFields.isEmpty()) 0f else filledCount.toFloat() / requiredFields.size

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.staff),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Add New Staff",
                            fontWeight = FontWeight.Bold,
                            color = Color.Blue
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.elevatedCardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Teacher Registration",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            text = "$filledCount / ${requiredFields.size} required fields completed",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Full Name
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { onStateChange(state.copy(name = it)) },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Age (auto, read-only) + DOB (clickable)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.age,
                                onValueChange = {},
                                label = { Text("Age (auto)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                readOnly = true,
                                enabled = false
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = state.dob,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Date of Birth") },
                                modifier = Modifier
                                    .weight(2f)
                                    .clickable { showDatePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                enabled = false,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Pick date"
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onStateChange(state.copy(phone = it)) },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Address
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { onStateChange(state.copy(email = it)) },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary Subject dropdown
                        var subjectExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = subjectExpanded,
                            onExpandedChange = { subjectExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.subject,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Primary Subject") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = subjectExpanded,
                                onDismissRequest = { subjectExpanded = false }
                            ) {
                                subjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject) },
                                        onClick = {
                                            onStateChange(state.copy(subject = subject))
                                            subjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Home Section dropdown
                        var sectionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sectionExpanded,
                            onExpandedChange = { sectionExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.homeSection,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Home Section") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = sectionExpanded,
                                onDismissRequest = { sectionExpanded = false }
                            ) {
                                sections.forEach { section ->
                                    DropdownMenuItem(
                                        text = { Text(section) },
                                        onClick = {
                                            onStateChange(state.copy(homeSection = section))
                                            sectionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Classes Taught (multi-select chips)
                        Text(
                            text = "Classes Taught",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            classes.forEach { className ->
                                FilterChip(
                                    selected = state.classesTaught.contains(className),
                                    onClick = {
                                        val newList = if (state.classesTaught.contains(className)) {
                                            state.classesTaught - className
                                        } else {
                                            state.classesTaught + className
                                        }
                                        onStateChange(state.copy(classesTaught = newList))
                                    },
                                    label = { Text(className) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Submit button (enabled only when all mandatory fields are filled)
                        Button(
                            onClick = onSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = filledCount == requiredFields.size
                        ) {
                            Text(
                                "Submit",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddStaffScreenPreview() {
    MarkMyDayTheme {
        AddStaffScreen(onBack = {}, onSubmit = {})
    }
}