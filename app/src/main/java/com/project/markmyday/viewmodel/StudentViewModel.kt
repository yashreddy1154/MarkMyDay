package com.project.markmyday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    object Success : StudentRegistrationState()
    data class Error(val message: String) : StudentRegistrationState()
}

/**
 * ViewModel for managing student-related operations, specifically registration.
 */
class StudentViewModel(
    private val repository: StudentRepository = StudentRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

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
     * Registers a new student by creating credentials in Firebase Auth 
     * and saving details in Firestore.
     */
    fun registerStudent(formState: AddStudentFormState) {
        Log.d("AddStudent", "ViewModel function triggered")
        viewModelScope.launch {
            _registrationState.value = StudentRegistrationState.Loading
            try {
                // 1. Generate Credentials
                val studentId = generateStudentId(formState.studentClass)
                val email = "${studentId.lowercase()}@markmyday.com"
                val password = formState.dob.filter { it.isDigit() }

                if (password.length != 8) {
                    throw Exception("Invalid DOB format for password. Expected 8 digits (ddMMyyyy).")
                }

                Log.d(TAG, "Attempting to register student: $studentId with email: $email")

                // 2. Create User in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to retrieve UID from Auth")

                Log.d(TAG, "Auth user created successfully with UID: $uid")

                // 3. Prepare Student object for Firestore 'students' collection
                val student = Student(
                    studentId = studentId,
                    name = formState.name,
                    age = formState.age.toIntOrNull() ?: 0,
                    dob = formState.dob,
                    parentName = formState.parentName,
                    phone = formState.phone,
                    studentClass = formState.studentClass,
                    section = formState.section,
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
                    "studentId" to studentId,
                    "studentClass" to formState.studentClass,
                    "section" to formState.section
                )
                firestore.collection("users").document(uid).set(userMap).await()

                Log.d(TAG, "Student data saved to Firestore successfully for ID: $studentId")
                _registrationState.value = StudentRegistrationState.Success

            } catch (e: Exception) {
                Log.e("FirebaseError", "Error: ${e.message}", e)
                _registrationState.value = StudentRegistrationState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    /**
     * Generates a unique Student ID.
     * Format: "S" + 4 random digits + "C" + ClassNumber (e.g., S4512C10).
     */
    fun generateStudentId(studentClass: String): String {
        val randomDigits = (1000..9999).random()
        val classNumber = studentClass.filter { it.isDigit() }
        val id = "S${randomDigits}C$classNumber"
        Log.d(TAG, "Generated Student ID: $id for class: $studentClass")
        return id
    }
}
