package com.project.markmyday.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class AttendanceSubmissionState {
    object Idle : AttendanceSubmissionState()
    object Loading : AttendanceSubmissionState()
    object Success : AttendanceSubmissionState()
    data class Error(val message: String) : AttendanceSubmissionState()
}

class AttendanceViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _assignedClasses = MutableStateFlow<List<String>>(emptyList())
    val assignedClasses: StateFlow<List<String>> = _assignedClasses.asStateFlow()

    private val _studentsByClass = MutableStateFlow<Map<String, List<Student>>>(emptyMap())
    val studentsByClass: StateFlow<Map<String, List<Student>>> = _studentsByClass.asStateFlow()

    private val _teacherId = MutableStateFlow("")
    private val _teacherSubject = MutableStateFlow("")

    val attendanceStates = mutableStateMapOf<String, Boolean>()

    private val _submissionState = MutableStateFlow<AttendanceSubmissionState>(AttendanceSubmissionState.Idle)
    val submissionState: StateFlow<AttendanceSubmissionState> = _submissionState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTeacherData()
    }

    private fun loadTeacherData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teacherDoc = firestore.collection("users").document(uid).get().await()
                val assignments = teacherDoc.get("teaching_assignments") as? List<String> ?: emptyList()
                val subject = teacherDoc.getString("subject") ?: ""
                val teacherId = teacherDoc.getString("teacherId") ?: ""

                _assignedClasses.value = assignments
                _teacherSubject.value = subject
                _teacherId.value = teacherId

                if (assignments.isNotEmpty()) {
                    fetchStudents(assignments)
                } else {
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _submissionState.value = AttendanceSubmissionState.Error(e.localizedMessage ?: "Failed to load teacher data")
            }
        }
    }

    private fun fetchStudents(assignments: List<String>) {
        firestore.collection("users")
            .whereEqualTo("role", "student")
            .whereIn("class_section", assignments)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val students = snapshot.documents.map { doc ->
                        Student(
                            uid = doc.getString("uid") ?: "",
                            studentId = doc.getString("studentId") ?: "",
                            name = doc.getString("name") ?: "",
                            studentClass = doc.getString("studentClass") ?: "",
                            section = doc.getString("section") ?: "",
                            classSection = doc.getString("class_section") ?: ""
                        )
                    }
                    _studentsByClass.value = students.groupBy { it.classSection }
                    
                    // Initialize attendance states for all students as true (Present)
                    students.forEach { student ->
                        if (!attendanceStates.containsKey(student.uid)) {
                            attendanceStates[student.uid] = true
                        }
                    }
                }
                _isLoading.value = false
            }
    }

    fun toggleAttendance(studentId: String, isPresent: Boolean) {
        attendanceStates[studentId] = isPresent
    }

    fun submitAttendance(classSection: String) {
        val studentsInClass = _studentsByClass.value[classSection] ?: return
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val subject = _teacherSubject.value
        val docId = "${dateStr}_${classSection.replace(" ", "_")}_$subject"

        val presentStudents = mutableListOf<String>()
        val absentStudents = mutableListOf<String>()

        studentsInClass.forEach { student ->
            if (attendanceStates[student.uid] == true) {
                presentStudents.add(student.uid)
            } else {
                absentStudents.add(student.uid)
            }
        }

        val attendanceData = hashMapOf(
            "date" to com.google.firebase.Timestamp.now(),
            "class_section" to classSection,
            "subject" to subject,
            "teacher_id" to _teacherId.value,
            "present_students" to presentStudents,
            "absent_students" to absentStudents
        )

        viewModelScope.launch {
            _submissionState.value = AttendanceSubmissionState.Loading
            try {
                firestore.collection("attendance").document(docId).set(attendanceData).await()
                _submissionState.value = AttendanceSubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = AttendanceSubmissionState.Error(e.localizedMessage ?: "Submission failed")
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = AttendanceSubmissionState.Idle
    }
}
