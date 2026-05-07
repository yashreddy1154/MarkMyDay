package com.project.markmyday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Student
import com.project.markmyday.data.repository.StudentRepository
import com.project.markmyday.ui.screens.AddStudentFormState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * States for the Student Registration process.
 */
sealed class StudentRegistrationState {
    object Idle : StudentRegistrationState()
    object Loading : StudentRegistrationState()
    data class Success(val admissionNo: String) : StudentRegistrationState()
    data class Error(val message: String) : StudentRegistrationState()
}

/**
 * ViewModel for managing student-related operations, specifically registration.
 */
class StudentViewModel(
    private val repository: StudentRepository = StudentRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    // Using a secondary FirebaseApp instance to prevent auto-login of newly created users
    private val auth: FirebaseAuth by lazy {
        val defaultApp = FirebaseApp.getInstance()
        val options = defaultApp.options
        val context = defaultApp.applicationContext
        val secondaryApp = try {
            FirebaseApp.getInstance("Secondary")
        } catch (e: Exception) {
            FirebaseApp.initializeApp(context, options, "Secondary")
        }
        FirebaseAuth.getInstance(secondaryApp)
    }

    private val _registrationState = MutableStateFlow<StudentRegistrationState>(StudentRegistrationState.Idle)
    val registrationState: StateFlow<StudentRegistrationState> = _registrationState.asStateFlow()

    /**
     * Exposes all students from the repository as a StateFlow.
     */
    val allStudents: StateFlow<List<Student>> = repository.getAllStudents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val TAG = "StudentViewModel"

    /**
     * Resets the registration state to Idle.
     */
    fun resetRegistrationState() {
        _registrationState.value = StudentRegistrationState.Idle
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(studentId)
            } catch (e: Exception) {
                _registrationState.value = StudentRegistrationState.Error(e.localizedMessage ?: "Delete failed")
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.updateStudent(student)
            } catch (e: Exception) {
                _registrationState.value = StudentRegistrationState.Error(e.localizedMessage ?: "Update failed")
            }
        }
    }

    fun registerStudent(formState: AddStudentFormState) {
        Log.d("AddStudent", "ViewModel function triggered")
        viewModelScope.launch {
            _registrationState.value = StudentRegistrationState.Loading
            try {
                // 1. Generate Credentials
                val admissionNo = generateUniqueAdmissionNo()
                val email = "${admissionNo.lowercase()}@gmail.com"
                val password = formState.dob.filter { it.isDigit() }

                if (password.length != 8) {
                    throw Exception("error_invalid_dob_format")
                }

                Log.d(TAG, "Attempting to register student: $admissionNo with email: $email")

                // 2. Create User in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID from Auth")

                Log.d(TAG, "Auth user created successfully with UID: $uid")

                // 3. Prepare Student object
                val studentClassInt = formState.studentClass.toIntOrNull() ?: 0
                val category = when (studentClassInt) {
                    in 1..5 -> "Primary"
                    in 6..7 -> "Secondary"
                    in 8..10 -> "High School"
                    else -> "Unknown"
                }

                val student = Student(
                    uid = uid,
                    studentId = admissionNo,
                    name = formState.name,
                    age = formState.age.toIntOrNull() ?: 0,
                    dob = formState.dob,
                    gender = formState.gender,
                    motherName = formState.motherName,
                    motherPhone = formState.motherPhone,
                    fatherName = formState.fatherName,
                    fatherPhone = formState.fatherPhone,
                    studentClass = formState.studentClass,
                    category = category,
                    email = email
                )

                // 4. Save to 'students' collection via Repository
                repository.addStudent(student)

                // 5. Save to 'users' collection for global role management
                val userMap = hashMapOf(
                    "uid" to uid,
                    "name" to formState.name,
                    "role" to "student",
                    "email" to email,
                    "studentId" to admissionNo,
                    "studentClass" to formState.studentClass,
                    "category" to category,
                    "gender" to formState.gender,
                    "motherName" to formState.motherName,
                    "motherPhone" to formState.motherPhone,
                    "fatherName" to formState.fatherName,
                    "fatherPhone" to formState.fatherPhone
                )
                firestore.collection("users").document(uid).set(userMap).await()

                Log.d(TAG, "Student data saved to Firestore successfully for ID: $admissionNo")
                _registrationState.value = StudentRegistrationState.Success(admissionNo)

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}", e)
                _registrationState.value = StudentRegistrationState.Error(e.localizedMessage ?: "error_registration_failed")
            }
        }
    }

    private suspend fun generateUniqueAdmissionNo(): String {
        var isUnique = false
        var generatedId = ""
        while (!isUnique) {
            val randomNum = (100000..999999).random()
            generatedId = randomNum.toString()
            
            // Check in 'students' collection
            val doc = firestore.collection("students").document(generatedId).get().await()
            if (!doc.exists()) {
                isUnique = true
            }
        }
        return generatedId
    }

    private fun generateStudentId(studentClass: String): String {
        return "" // Deprecated by admissionNo logic
    }
}
