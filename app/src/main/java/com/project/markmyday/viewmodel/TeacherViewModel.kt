package com.project.markmyday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.data.repository.TeacherRepository
import com.project.markmyday.ui.screens.AddStaffFormState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class TeacherRegistrationState {
    object Idle : TeacherRegistrationState()
    object Loading : TeacherRegistrationState()
    data class Success(val teacherId: String) : TeacherRegistrationState()
    data class Error(val message: String) : TeacherRegistrationState()
}

class TeacherViewModel(
    private val repository: TeacherRepository = TeacherRepository(),
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

    private val _registrationState = MutableStateFlow<TeacherRegistrationState>(TeacherRegistrationState.Idle)
    val registrationState: StateFlow<TeacherRegistrationState> = _registrationState.asStateFlow()

    val allTeachers: StateFlow<List<Teacher>> = repository.getAllTeachers()
        .catch { e ->
            Log.e("TeacherViewModel", "Error fetching teachers: ${e.message}")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _homeSectionStudents = MutableStateFlow<List<com.project.markmyday.data.model.Student>>(emptyList())
    val homeSectionStudents: StateFlow<List<com.project.markmyday.data.model.Student>> = _homeSectionStudents.asStateFlow()

    fun fetchHomeSectionStudents(homeSection: String) {
        if (homeSection == "N/A") return
        
        viewModelScope.launch {
            try {
                // Normalize class: remove "Class ", replace "+" with " ", and trim
                val normalizedClass = homeSection.replace("Class ", "")
                    .replace("+", " ")
                    .trim()
                
                Log.d("TeacherViewModel", "Fetching students for class: '$normalizedClass' (from '$homeSection')")

                firestore.collection("users")
                    .whereEqualTo("role", "student")
                    .whereEqualTo("studentClass", normalizedClass)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) return@addSnapshotListener
                        if (snapshot != null) {
                            val students = snapshot.documents.map { doc ->
                                com.project.markmyday.data.model.Student(
                                    uid = doc.getString("uid") ?: "",
                                    studentId = doc.getString("studentId") ?: "",
                                    name = doc.getString("name") ?: "",
                                    studentClass = doc.getString("studentClass") ?: "",
                                    motherName = doc.getString("motherName") ?: "",
                                    fatherName = doc.getString("fatherName") ?: "",
                                    motherPhone = doc.getString("motherPhone") ?: "",
                                    fatherPhone = doc.getString("fatherPhone") ?: ""
                                )
                            }
                            _homeSectionStudents.value = students
                        }
                    }
            } catch (e: Exception) {
                Log.e("TeacherViewModel", "Error fetching home students: ${e.message}")
            }
        }
    }

    fun resetRegistrationState() {
        _registrationState.value = TeacherRegistrationState.Idle
    }

    fun deleteTeacher(teacherId: String) {
        viewModelScope.launch {
            try {
                // 1. Find and delete from 'users' collection using teacherId
                val usersQuery = firestore.collection("users")
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()
                
                for (document in usersQuery.documents) {
                    firestore.collection("users").document(document.id).delete().await()
                }

                // 2. Delete from 'teachers' collection
                repository.deleteTeacher(teacherId)

            } catch (e: Exception) {
                _registrationState.value = TeacherRegistrationState.Error(e.localizedMessage ?: "Delete failed")
            }
        }
    }

    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            try {
                repository.updateTeacher(teacher)
            } catch (e: Exception) {
                _registrationState.value = TeacherRegistrationState.Error(e.localizedMessage ?: "Update failed")
            }
        }
    }

    fun registerTeacher(formState: AddStaffFormState) {
        viewModelScope.launch {
            _registrationState.value = TeacherRegistrationState.Loading
            try {
                val teacherId = generateUniqueTeacherId()
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
                    gender = formState.gender,
                    subject = formState.subject,
                    homeSection = formState.homeSection,
                    classesTaughtCategories = formState.classesTaughtCategories,
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
                    "home_section" to formState.homeSection,
                    "subject" to formState.subject,
                    "teaching_assignments" to formState.classesTaught,
                    "dob" to formState.dob,
                    "password" to password
                )
                firestore.collection("users").document(uid).set(userMap).await()

                _registrationState.value = TeacherRegistrationState.Success(teacherId)
            } catch (e: Exception) {
                android.util.Log.e("TeacherViewModel", "Registration failed", e)
                _registrationState.value = TeacherRegistrationState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    private suspend fun generateUniqueTeacherId(): String {
        var isUnique = false
        var generatedId = ""
        while (!isUnique) {
            val randomNum = (1000..9999).random()
            generatedId = "T$randomNum"
            
            // Check in 'teachers' collection
            val doc = firestore.collection("teachers").document(generatedId).get().await()
            if (!doc.exists()) {
                isUnique = true
            }
        }
        return generatedId
    }

    private fun generateEmail(teacherId: String): String {
        // Requirements: teacherId + "@gmail.com" (lowercase t)
        return "${teacherId.lowercase()}@gmail.com"
    }
}
