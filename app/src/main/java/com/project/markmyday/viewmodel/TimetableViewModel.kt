package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Student
import com.project.markmyday.data.model.Teacher
import com.project.markmyday.data.model.Timetable
import com.project.markmyday.data.repository.StudentRepository
import com.project.markmyday.data.repository.TeacherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class TimetableState {
    object Idle : TimetableState()
    object Loading : TimetableState()
    object Success : TimetableState()
    data class Error(val message: String) : TimetableState()
}

class TimetableViewModel(
    private val teacherRepository: TeacherRepository = TeacherRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val _state = MutableStateFlow<TimetableState>(TimetableState.Idle)
    val state: StateFlow<TimetableState> = _state.asStateFlow()

    private val _allTeachers = MutableStateFlow<List<Teacher>>(emptyList())
    val allTeachers: StateFlow<List<Teacher>> = _allTeachers.asStateFlow()

    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents: StateFlow<List<Student>> = _allStudents.asStateFlow()

    // Key: Class name (e.g., "Class 1"), Value: Selected Teacher
    private val _classTeacherAssignments = MutableStateFlow<Map<String, Teacher?>>(
        (1..10).associate { "Class $it" to null }
    )
    val classTeacherAssignments: StateFlow<Map<String, Teacher?>> = _classTeacherAssignments.asStateFlow()

    // Key: Class name, Value: List of Student IDs
    private val _classStudentAssignments = MutableStateFlow<Map<String, List<String>>>(
        (1..10).associate { "Class $it" to emptyList() }
    )
    val classStudentAssignments: StateFlow<Map<String, List<String>>> = _classStudentAssignments.asStateFlow()

    init {
        fetchTeachers()
        fetchStudents()
        loadExistingAssignments()
    }

    private fun fetchTeachers() {
        viewModelScope.launch {
            teacherRepository.getAllTeachers().collect { teachers ->
                _allTeachers.value = teachers
            }
        }
    }

    private fun fetchStudents() {
        viewModelScope.launch {
            studentRepository.getAllStudents().collect { students ->
                _allStudents.value = students
                
                // Initialize students for each class if not already loaded from Firestore
                val currentAssignments = _classStudentAssignments.value.toMutableMap()
                (1..10).forEach { i ->
                    val className = "Class $i"
                    if (currentAssignments[className].isNullOrEmpty()) {
                        val classStudents = students.filter { it.studentClass == i.toString() }
                        currentAssignments[className] = classStudents.map { it.studentId }
                    }
                }
                _classStudentAssignments.value = currentAssignments
            }
        }
    }

    private fun loadExistingAssignments() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("timetable").get().await()
                val existing = snapshot.toObjects(Timetable::class.java)
                val currentTeacherAssignments = _classTeacherAssignments.value.toMutableMap()
                val currentStudentAssignments = _classStudentAssignments.value.toMutableMap()
                
                existing.forEach { timetable ->
                    val teacher = _allTeachers.value.find { it.teacherId == timetable.homeTeacherId }
                    if (teacher != null) {
                        currentTeacherAssignments[timetable.className] = teacher
                    }
                    if (timetable.studentList.isNotEmpty()) {
                        currentStudentAssignments[timetable.className] = timetable.studentList
                    }
                }
                _classTeacherAssignments.value = currentTeacherAssignments
                _classStudentAssignments.value = currentStudentAssignments
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun assignTeacherToClass(className: String, teacher: Teacher?) {
        val currentMap = _classTeacherAssignments.value.toMutableMap()
        
        // Rule: One teacher must have only one home section.
        // If the teacher is already selected for another class, deselect them for other class.
        if (teacher != null) {
            currentMap.entries.forEach { (cls, t) ->
                if (t?.teacherId == teacher.teacherId) {
                    currentMap[cls] = null
                }
            }
        }
        
        // Rule: One class must only have one home teacher.
        currentMap[className] = teacher
        _classTeacherAssignments.value = currentMap
    }

    fun updateClassStudents(className: String, studentIds: List<String>) {
        val currentMap = _classStudentAssignments.value.toMutableMap()
        currentMap[className] = studentIds
        _classStudentAssignments.value = currentMap
    }

    fun nextStep() {
        if (_currentStep.value < 3) {
            when (_currentStep.value) {
                1 -> saveStep1Data()
                2 -> saveStep2Data()
                else -> _currentStep.value += 1
            }
        }
    }

    private fun saveStep1Data() {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val assignments = _classTeacherAssignments.value
                
                // Update 'timetable' and 'teachers'
                assignments.forEach { (className, teacher) ->
                    if (teacher != null) {
                        // We use merge here to preserve studentIds if they exist
                        val timetableData = hashMapOf(
                            "className" to className,
                            "homeTeacherId" to teacher.teacherId,
                            "homeTeacherName" to teacher.name
                        )
                        firestore.collection("timetable").document(className)
                            .set(timetableData, com.google.firebase.firestore.SetOptions.merge()).await()
                        
                        // 2. Update 'teachers' collection
                        val updatedTeacher = teacher.copy(homeSection = className)
                        teacherRepository.updateTeacher(updatedTeacher)

                        // 3. Update 'users' collection
                        val userQuery = firestore.collection("users")
                            .whereEqualTo("teacherId", teacher.teacherId)
                            .get()
                            .await()
                        
                        for (doc in userQuery.documents) {
                            firestore.collection("users").document(doc.id)
                                .update("home_section", className).await()
                        }
                    }
                }
                
                _state.value = TimetableState.Success
                _currentStep.value = 2
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save assignments")
            }
        }
    }

    private fun saveStep2Data() {
        viewModelScope.launch {
            _state.value = TimetableState.Loading
            try {
                val studentAssignments = _classStudentAssignments.value
                
                studentAssignments.forEach { (className, studentIds) ->
                    if (studentIds.isNotEmpty()) {
                        val data = mapOf("studentList" to studentIds)
                        firestore.collection("timetable").document(className)
                            .set(data, com.google.firebase.firestore.SetOptions.merge()).await()
                    }
                }
                
                _state.value = TimetableState.Success
                _currentStep.value = 3
            } catch (e: Exception) {
                _state.value = TimetableState.Error(e.localizedMessage ?: "Failed to save student list")
            }
        }
    }

    fun goToStep(step: Int) {
        if (step in 1..3 && step <= _currentStep.value) {
            _currentStep.value = step
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
        }
    }

    fun resetState() {
        _state.value = TimetableState.Idle
    }
}
