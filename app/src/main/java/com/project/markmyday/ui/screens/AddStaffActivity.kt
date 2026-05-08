package com.project.markmyday.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.LocalSettingsViewModel
import com.project.markmyday.R
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.viewmodel.SettingsViewModel
import com.project.markmyday.viewmodel.TeacherRegistrationState
import com.project.markmyday.viewmodel.TeacherViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AddStaffActivity : AppCompatActivity() {
    
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val teacherViewModel: TeacherViewModel = viewModel()

            CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
                MarkMyDayTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AddStaffScreen(
                            onBack = { finish() },
                            onSubmit = { formState ->
                                teacherViewModel.registerTeacher(formState)
                            },
                            viewModel = teacherViewModel
                        )
                    }
                }
            }
        }
    }
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

    // Show success Dialog and reset form
    LaunchedEffect(registrationState) {
        if (registrationState is TeacherRegistrationState.Success) {
            val teacherId = (registrationState as TeacherRegistrationState.Success).teacherId
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.teacher_registration))
                .setMessage(context.getString(R.string.staff_added_success, teacherId))
                .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                    state = AddStaffFormState()
                    viewModel.resetRegistrationState()
                    dialog.dismiss()
                }
                .show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddStaffContent(
            title = stringResource(R.string.add_new_staff),
            state = state,
            onStateChange = { state = it },
            onBack = onBack,
            onSubmit = { onSubmit(state) },
            registrationState = registrationState
        )

        // Error dialog
        if (registrationState is TeacherRegistrationState.Error) {
            val errorMessage = (registrationState as TeacherRegistrationState.Error).message
            AlertDialog(
                onDismissRequest = { /* Keep showing until fixed */ },
                title = { Text(stringResource(R.string.error_title)) },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { /* Optionally reset error state in ViewModel */ }) {
                        Text(stringResource(R.string.ok))
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
    val gender: String = "",
    val phone: String = "",
    val email: String = "",
    val subject: String = "",
    val homeSection: String = "",
    val classesTaught: List<String> = emptyList(),
    val classesTaughtCategories: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffContent(
    title: String,
    state: AddStaffFormState,
    onStateChange: (AddStaffFormState) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    registrationState: TeacherRegistrationState
) {
    val context = LocalContext.current
    val subjects = listOf(
        stringResource(R.string.subject_telugu),
        stringResource(R.string.subject_hindi),
        stringResource(R.string.subject_english),
        stringResource(R.string.subject_math),
        stringResource(R.string.subject_science),
        stringResource(R.string.subject_social),
        stringResource(R.string.subject_physics),
        stringResource(R.string.subject_biology),
        stringResource(R.string.subject_japanese)
    )
    val categories = listOf(
        stringResource(R.string.primary),
        stringResource(R.string.secondary),
        stringResource(R.string.high_school)
    )
    val ageLimitError = stringResource(R.string.age_limit_error)

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
                                    ageLimitError,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Form completion progress (7 mandatory fields)
    val requiredFields = listOf(
        state.name.isNotBlank(),
        state.dob.isNotBlank(),
        state.gender.isNotBlank(),
        state.phone.isNotBlank(),
        state.subject.isNotBlank(),
        state.classesTaughtCategories.isNotEmpty()
    )
    val filledCount = requiredFields.count { it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Full Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { onStateChange(state.copy(name = it)) },
                label = { Text(stringResource(R.string.full_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DOB + Gender side by side (approx)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.dob,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.date_of_birth)) },
                    modifier = Modifier
                        .weight(1.5f)
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    enabled = false,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.pick_date)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = state.gender == "Male",
                            onClick = { onStateChange(state.copy(gender = "Male")) }
                        )
                        Text(text = stringResource(R.string.male), modifier = Modifier.clickable { onStateChange(state.copy(gender = "Male")) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = state.gender == "Female",
                            onClick = { onStateChange(state.copy(gender = "Female")) }
                        )
                        Text(text = stringResource(R.string.female), modifier = Modifier.clickable { onStateChange(state.copy(gender = "Female")) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number
            OutlinedTextField(
                value = state.phone,
                onValueChange = { onStateChange(state.copy(phone = it)) },
                label = { Text(stringResource(R.string.phone_number)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    label = { Text(stringResource(R.string.primary_subject)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(16.dp))

            // Classes Taught categories
            Text(
                text = stringResource(R.string.classes_taught),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = state.classesTaughtCategories.contains(category),
                        onClick = {
                            val newList = if (state.classesTaughtCategories.contains(category)) {
                                state.classesTaughtCategories - category
                            } else {
                                state.classesTaughtCategories + category
                            }
                            onStateChange(state.copy(classesTaughtCategories = newList))
                        },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = filledCount == requiredFields.size && registrationState !is TeacherRegistrationState.Loading
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (registrationState is TeacherRegistrationState.Loading) {
                        Text(
                            stringResource(R.string.adding_to_database),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                        )
                    } else {
                        Text(
                            stringResource(R.string.submit),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Helper functions for date & age
private fun calculateAgeFromDateOfBirth(dateOfBirth: Date?): Int? {
    if (dateOfBirth == null) return null
    val today = Calendar.getInstance()
    val dobCal = Calendar.getInstance().apply { time = dateOfBirth }
    var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
    if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
        age--
    }
    return age
}

private fun formatDateForDisplay(date: Date?): String {
    if (date == null) return ""
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(date.toInstant())
}

@Preview(showBackground = true)
@Composable
fun AddStaffScreenPreview() {
    MarkMyDayTheme {
        AddStaffScreen(onBack = {}, onSubmit = {})
    }
}
