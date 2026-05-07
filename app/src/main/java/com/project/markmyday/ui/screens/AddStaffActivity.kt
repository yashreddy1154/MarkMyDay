package com.project.markmyday.ui.screens

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

// --- Combined UI Components from AddStaff.kt ---

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
    val staffAddedSuccess = stringResource(R.string.staff_added_success)

    // Show success Toast and reset form
    LaunchedEffect(registrationState) {
        if (registrationState is TeacherRegistrationState.Success) {
            Toast.makeText(context, staffAddedSuccess, Toast.LENGTH_LONG).show()
            state = AddStaffFormState()
            viewModel.resetRegistrationState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddStaffContent(
            title = stringResource(R.string.add_new_staff),
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
    val phone: String = "",
    val email: String = "",
    val subject: String = "",
    val homeSection: String = "",
    val classesTaught: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStaffContent(
    title: String,
    state: AddStaffFormState,
    onStateChange: (AddStaffFormState) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val subjects = listOf("Telugu", "Hindi", "English", "Maths", "Science", "Physics", "Biology", "Computers", "Social", "Games/PT")
    val sections = listOf("1A", "1B", "2A", "2B", "3A", "3B", "4A", "4B", "5A", "5B", "6A", "6B", "7A", "7B", "8A", "8B", "9A", "9B", "10A", "10B")
    val classes = (1..10).map { stringResource(R.string.class_format, it) }
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
                CenterAlignedTopAppBar(
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
                                contentDescription = stringResource(R.string.back),
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
                        .fillMaxHeight()
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
                            text = stringResource(R.string.teacher_registration),
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
                            text = stringResource(R.string.fields_completed, filledCount, requiredFields.size),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Full Name
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { onStateChange(state.copy(name = it)) },
                            label = { Text(stringResource(R.string.full_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Age (auto, read-only) + DOB (clickable)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.age,
                                onValueChange = {},
                                label = { Text(stringResource(R.string.age_auto)) },
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
                                label = { Text(stringResource(R.string.date_of_birth)) },
                                modifier = Modifier
                                    .weight(2f)
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
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onStateChange(state.copy(phone = it)) },
                            label = { Text(stringResource(R.string.phone_number)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email Address
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { onStateChange(state.copy(email = it)) },
                            label = { Text(stringResource(R.string.email_address)) },
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
                                label = { Text(stringResource(R.string.primary_subject)) },
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
                                label = { Text(stringResource(R.string.home_section)) },
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
                            text = stringResource(R.string.classes_taught),
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
                            classes.forEachIndexed { index, className ->
                                val classValue = (index + 1).toString()
                                FilterChip(
                                    selected = state.classesTaught.contains(classValue),
                                    onClick = {
                                        val newList = if (state.classesTaught.contains(classValue)) {
                                            state.classesTaught - classValue
                                        } else {
                                            state.classesTaught + classValue
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
                                stringResource(R.string.submit),
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
