package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.data.repository.TeacherRepository
import com.project.markmyday.ui.screens.AddStaffFormState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class TeacherRegistrationState {
    object Idle : TeacherRegistrationState()
    object Loading : TeacherRegistrationState()
    object Success : TeacherRegistrationState()
    data class Error(val message: String) : TeacherRegistrationState()
}

class TeacherViewModel(
    private val repository: TeacherRepository = TeacherRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _registrationState = MutableStateFlow<TeacherRegistrationState>(TeacherRegistrationState.Idle)
    val registrationState: StateFlow<TeacherRegistrationState> = _registrationState.asStateFlow()

    fun registerTeacher(formState: AddStaffFormState) {
        viewModelScope.launch {
            _registrationState.value = TeacherRegistrationState.Loading
            try {
                val subjectCode = getSubjectCode(formState.subject)
                val teacherId = generateTeacherId(subjectCode)
                val email = generateEmail(teacherId)
                val password = formState.dob.filter { it.isDigit() } // Ensure password is digits only (ddmmyyyy)

                if (password.length != 8) {
                    _registrationState.value = TeacherRegistrationState.Error("Invalid DOB format. Expected ddmmyyyy.")
                    return@launch
                }

                // 1. Create User in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw Exception("Failed to get UID")

                // 2. Prepare Teacher object for Firestore
                val teacher = Teacher(
                    teacherId = teacherId,
                    name = formState.name,
                    age = formState.age.toIntOrNull() ?: 0,
                    dob = formState.dob,
                    subject = formState.subject,
                    homeSection = formState.homeSection,
                    classesTaught = formState.classesTaught,
                    phone = formState.phone,
                    email = email
                )

                // 3. Save to Detailed 'teachers' collection
                repository.addTeacher(teacher)

                // 4. Save to General 'users' collection for Login handling
                val userMap = hashMapOf(
                    "uid" to uid,
                    "name" to formState.name,
                    "role" to "teacher",
                    "email" to email,
                    "teacherId" to teacherId,
                    "homeSection" to formState.homeSection,
                    "subject" to formState.subject
                )
                firestore.collection("users").document(uid).set(userMap).await()

                _registrationState.value = TeacherRegistrationState.Success
            } catch (e: Exception) {
                _registrationState.value = TeacherRegistrationState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    private fun generateTeacherId(subjectCode: String): String {
        // Requirements: "T1261" + SubjectCode
        return "T1261$subjectCode"
    }

    private fun generateEmail(teacherId: String): String {
        // Requirements: teacherId + "@gmail.com"
        return "${teacherId.lowercase()}@gmail.com"
    }

    private fun getSubjectCode(subjectName: String): String {
        return when (subjectName) {
            "Telugu" -> "TEL"
            "Hindi" -> "HIN"
            "English" -> "ENG"
            "Maths" -> "MAT"
            "Science" -> "SCI"
            "Physics" -> "PHY"
            "Biology" -> "BIO"
            "Computers" -> "COM"
            "Social" -> "SOC"
            "Games/PT" -> "GPT"
            else -> "GEN" // General/Unknown
        }
    }
}
