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

    // Show success Dialog and reset form
    LaunchedEffect(registrationState) {
        if (registrationState is StudentRegistrationState.Success) {
            val admissionNo = (registrationState as StudentRegistrationState.Success).admissionNo
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.student_registration))
                .setMessage(context.getString(R.string.student_added_success, admissionNo))
                .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                    state = AddStudentFormState()
                    viewModel.resetRegistrationState()
                    dialog.dismiss()
                }
                .show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddStudentContent(
            title = stringResource(R.string.add_new_student),
            state = state,
            onStateChange = { state = it },
            onBack = onBack,
            onSubmit = { onSubmit(state) },
            registrationState = registrationState
        )
    }
}

data class AddStudentFormState(
    val name: String = "",
    val age: String = "",
    val dob: String = "",
    val gender: String = "",
    val motherName: String = "",
    val motherPhone: String = "",
    val fatherName: String = "",
    val fatherPhone: String = "",
    val studentClass: String = "",
    val email: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentContent(
    title: String,
    state: AddStudentFormState,
    onStateChange: (AddStudentFormState) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    registrationState: StudentRegistrationState
) {
    val context = LocalContext.current
    val classes = (1..10).map { it.toString() }
    
    val errorEnterStudentName = stringResource(R.string.error_enter_student_name)
    val errorSelectClass = stringResource(R.string.error_select_class)
    val errorSelectDob = stringResource(R.string.error_select_dob)
    val errorParentDetails = stringResource(R.string.error_parent_details)

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

    // Form validation
    val isFormValid = state.name.isNotBlank() && 
                      state.dob.isNotBlank() && 
                      state.studentClass.isNotBlank() &&
                      ((state.motherName.isNotBlank() && state.motherPhone.isNotBlank()) || 
                       (state.fatherName.isNotBlank() && state.fatherPhone.isNotBlank()))

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
                label = { Text(stringResource(R.string.student_name)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DOB + Gender
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.dob,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.dob_label)) },
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

            // Mother details
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.motherName,
                    onValueChange = { onStateChange(state.copy(motherName = it)) },
                    label = { Text(stringResource(R.string.mother_name)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.motherPhone,
                    onValueChange = { onStateChange(state.copy(motherPhone = it)) },
                    label = { Text(stringResource(R.string.mother_phone)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Father details
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.fatherName,
                    onValueChange = { onStateChange(state.copy(fatherName = it)) },
                    label = { Text(stringResource(R.string.father_name)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = state.fatherPhone,
                    onValueChange = { onStateChange(state.copy(fatherPhone = it)) },
                    label = { Text(stringResource(R.string.father_phone)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false }
                ) {
                    classes.forEach { classValue ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.class_format, classValue.toInt())) },
                            onClick = {
                                onStateChange(state.copy(studentClass = classValue))
                                classExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit Button
            Button(
                onClick = {
                    if (isFormValid) {
                        onSubmit()
                    } else if (state.name.isBlank()) {
                        Toast.makeText(context, errorEnterStudentName, Toast.LENGTH_SHORT).show()
                    } else if (state.dob.isBlank()) {
                        Toast.makeText(context, errorSelectDob, Toast.LENGTH_SHORT).show()
                    } else if (state.studentClass.isBlank()) {
                        Toast.makeText(context, errorSelectClass, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, errorParentDetails, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = registrationState !is StudentRegistrationState.Loading
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (registrationState is StudentRegistrationState.Loading) {
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

            Spacer(modifier = Modifier.height(24.dp))
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
