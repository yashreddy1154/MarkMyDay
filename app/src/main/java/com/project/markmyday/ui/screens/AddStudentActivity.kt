package com.project.markmyday.ui.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.markmyday.LocalSettingsViewModel
import com.project.markmyday.R
import com.project.markmyday.ui.theme.MarkMyDayTheme
import com.project.markmyday.ui.utils.calculateAgeFromDateOfBirth
import com.project.markmyday.ui.utils.formatDateForDisplay
import com.project.markmyday.viewmodel.SettingsViewModel
import com.project.markmyday.viewmodel.StudentRegistrationState
import com.project.markmyday.viewmodel.StudentViewModel
import java.util.*

class AddStudentActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
            val studentViewModel: StudentViewModel = viewModel()

            CompositionLocalProvider(LocalSettingsViewModel provides settingsViewModel) {
                MarkMyDayTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AddStudentScreen(
                            onBack = { finish() },
                            onSubmit = { formState ->
                                studentViewModel.registerStudent(formState)
                            },
                            viewModel = studentViewModel
                        )
                    }
                }
            }
        }
    }
}

// --- Combined UI Components from AddStudentScreen.kt ---

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
    val studentAddedSuccess = stringResource(R.string.student_added_success)

    LaunchedEffect(registrationState) {
        if (registrationState is StudentRegistrationState.Success) {
            Toast.makeText(context, studentAddedSuccess, Toast.LENGTH_LONG).show()
            state = AddStudentFormState()
            viewModel.resetRegistrationState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddStudentContent(
            title = stringResource(R.string.add_new_student),
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
            val errorKey = (registrationState as StudentRegistrationState.Error).message
            val displayMessage = when(errorKey) {
                "error_invalid_dob_format" -> stringResource(R.string.error_invalid_dob_format)
                "error_registration_failed" -> stringResource(R.string.error_registration_failed)
                else -> errorKey
            }
            AlertDialog(
                onDismissRequest = { /* Handle error dismissal if needed */ },
                title = { Text(stringResource(R.string.registration_error)) },
                text = { Text(displayMessage) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetRegistrationState() }) {
                        Text(stringResource(R.string.ok))
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
    val classes = (1..10).map { stringResource(R.string.class_format, it) }
    val sections = listOf("A", "B", "C")
    
    val errorEnterStudentName = stringResource(R.string.error_enter_student_name)
    val errorSelectClass = stringResource(R.string.error_select_class)
    val errorSelectDob = stringResource(R.string.error_select_dob)

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
                            text = stringResource(R.string.student_registration),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Full Name
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { onStateChange(state.copy(name = it)) },
                            label = { Text(stringResource(R.string.student_name)) },
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
                                label = { Text(stringResource(R.string.dob_label)) },
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
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedTextField(
                                value = state.age,
                                onValueChange = { onStateChange(state.copy(age = it)) },
                                label = { Text(stringResource(R.string.age)) },
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
                            label = { Text(stringResource(R.string.parent_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Phone Number
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onStateChange(state.copy(phone = it)) },
                            label = { Text(stringResource(R.string.phone_number)) },
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
                            label = { Text(stringResource(R.string.email_optional)) },
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
                                label = { Text(stringResource(R.string.select_class)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = classExpanded,
                                onDismissRequest = { classExpanded = false }
                            ) {
                                classes.forEachIndexed { index, className ->
                                    val classValue = (index + 1).toString()
                                    DropdownMenuItem(
                                        text = { Text(className) },
                                        onClick = {
                                            onStateChange(state.copy(studentClass = classValue))
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
                                label = { Text(stringResource(R.string.select_section)) },
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
                                    Toast.makeText(context, errorEnterStudentName, Toast.LENGTH_SHORT).show()
                                } else if (state.studentClass.isBlank()) {
                                    Toast.makeText(context, errorSelectClass, Toast.LENGTH_SHORT).show()
                                } else if (state.dob.isBlank()) {
                                    Toast.makeText(context, errorSelectDob, Toast.LENGTH_SHORT).show()
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

@Preview(showBackground = true)
@Composable
fun AddStudentScreenPreview() {
    MarkMyDayTheme {
        AddStudentScreen(onBack = {}, onSubmit = {})
    }
}
