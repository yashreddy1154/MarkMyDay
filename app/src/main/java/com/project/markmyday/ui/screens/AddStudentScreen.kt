package com.project.markmyday.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.R
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.ui.utils.calculateAgeFromDateOfBirth
import com.project.markmyday.ui.utils.formatDateForDisplay
import com.project.markmyday.viewmodel.StudentRegistrationState
import com.project.markmyday.viewmodel.StudentViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    onBack: () -> Unit,
    onSubmit: (AddStudentFormState) -> Unit,
    viewModel: StudentViewModel = viewModel()
) {
    var state by remember { mutableStateOf(AddStudentFormState()) }
    val registrationState by viewModel.registrationState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(registrationState) {
        if (registrationState is StudentRegistrationState.Success) {
            Toast.makeText(context, "Student added successfully!", Toast.LENGTH_LONG).show()
            // Reset form state and viewmodel state to allow adding another student
            state = AddStudentFormState()
            viewModel.resetRegistrationState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddStudentContent(
            title = "Add New Student",
            state = state,
            onStateChange = { state = it },
            onBack = onBack,
            onSubmit = { onSubmit(state) }
        )

        if (registrationState is StudentRegistrationState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (registrationState is StudentRegistrationState.Error) {
            val errorMessage = (registrationState as StudentRegistrationState.Error).message
            AlertDialog(
                onDismissRequest = { /* Handle error dismissal if needed */ },
                title = { Text("Registration Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { /* Reset state if needed */ }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

data class AddStudentFormState(
    val name: String = "",
    val age: String = "",
    val dob: String = "",
    val parentName: String = "",
    val phone: String = "",
    val studentClass: String = "",
    val section: String = "",
    val email: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentContent(
    title: String,
    state: AddStudentFormState,
    onStateChange: (AddStudentFormState) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val classes = (1..10).map { "Class $it" }
    val sections = listOf("A", "B", "C")

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Date(millis)
                            val age = calculateAgeFromDateOfBirth(selectedDate)
                            val formattedDate = formatDateForDisplay(selectedDate)
                            onStateChange(
                                state.copy(
                                    dob = formattedDate,
                                    age = age?.toString() ?: ""
                                )
                            )
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image (Reusing staff bg for consistency or use a generic one)
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
                            title,
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
                        .fillMaxHeight(0.9f)
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
                            text = "Student Registration",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Full Name
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { onStateChange(state.copy(name = it)) },
                            label = { Text("Student Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // DOB and Age
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.dob,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("DOB (ddmmyyyy)") },
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
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = state.age,
                                onValueChange = { onStateChange(state.copy(age = it)) },
                                label = { Text("Age") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Parent Name
                        OutlinedTextField(
                            value = state.parentName,
                            onValueChange = { onStateChange(state.copy(parentName = it)) },
                            label = { Text("Parent Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onStateChange(state.copy(phone = it)) },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email (Optional based on requirements but good to have)
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { onStateChange(state.copy(email = it)) },
                            label = { Text("Email (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Class Dropdown
                        var classExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = classExpanded,
                            onExpandedChange = { classExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.studentClass,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Class") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = classExpanded,
                                onDismissRequest = { classExpanded = false }
                            ) {
                                classes.forEach { className ->
                                    DropdownMenuItem(
                                        text = { Text(className) },
                                        onClick = {
                                            onStateChange(state.copy(studentClass = className))
                                            classExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Section Dropdown
                        var sectionExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = sectionExpanded,
                            onExpandedChange = { sectionExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.section,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Section") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = sectionExpanded,
                                onDismissRequest = { sectionExpanded = false }
                            ) {
                                sections.forEach { sectionName ->
                                    DropdownMenuItem(
                                        text = { Text(sectionName) },
                                        onClick = {
                                            onStateChange(state.copy(section = sectionName))
                                            sectionExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                Log.d("AddStudent", "Submit clicked")
                                if (state.name.isBlank()) {
                                    Toast.makeText(context, "Please enter Student Name", Toast.LENGTH_SHORT).show()
                                } else if (state.studentClass.isBlank()) {
                                    Toast.makeText(context, "Please select a Class", Toast.LENGTH_SHORT).show()
                                } else if (state.dob.isBlank()) {
                                    Toast.makeText(context, "Please select Date of Birth", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSubmit()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
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
fun AddStudentScreenPreview() {
    MarkMyDayTheme {
        AddStudentScreen(onBack = {}, onSubmit = {})
    }
}
