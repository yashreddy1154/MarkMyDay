package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeacherHomeViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    
    val students: StateFlow<List<Student>> = combine(_allStudents, _searchQuery) { students, query ->
        if (query.isBlank()) {
            students
        } else {
            students.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHomeSectionStudents()
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun loadHomeSectionStudents() {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Get teacher's home_section
                val teacherDoc = firestore.collection("users").document(uid).get().await()
                val homeSection = teacherDoc.getString("home_section")
                
                if (homeSection != null) {
                    // Normalize homeSection for query
                    val normalizedClass = homeSection.replace("Class ", "").replace("+", " ").trim()
                    
                    // 2. Query students in that section
                    firestore.collection("users")
                        .whereEqualTo("role", "student")
                        .whereEqualTo("studentClass", normalizedClass)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                // Handle error
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                val studentList = snapshot.documents.map { doc ->
                                    Student(
                                        uid = doc.getString("uid") ?: "",
                                        studentId = doc.getString("studentId") ?: "",
                                        name = doc.getString("name") ?: "",
                                        studentClass = doc.getString("studentClass") ?: "",
                                        email = doc.getString("email") ?: "",
                                        motherName = doc.getString("motherName") ?: "",
                                        fatherName = doc.getString("fatherName") ?: "",
                                        motherPhone = doc.getString("motherPhone") ?: "",
                                        fatherPhone = doc.getString("fatherPhone") ?: ""
                                    )
                                }
                                _allStudents.value = studentList
                            }
                            _isLoading.value = false
                        }
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}
