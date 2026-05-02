package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

sealed class AssignmentSubmissionState {
    object Idle : AssignmentSubmissionState()
    object Loading : AssignmentSubmissionState()
    object Success : AssignmentSubmissionState()
    data class Error(val message: String) : AssignmentSubmissionState()
}

class AssignmentViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _assignedClasses = MutableStateFlow<List<String>>(emptyList())
    val assignedClasses: StateFlow<List<String>> = _assignedClasses.asStateFlow()

    private val _submissionState = MutableStateFlow<AssignmentSubmissionState>(AssignmentSubmissionState.Idle)
    val submissionState: StateFlow<AssignmentSubmissionState> = _submissionState.asStateFlow()

    private var teacherSubject: String = ""
    private var teacherId: String = ""

    init {
        loadTeacherData()
    }

    private fun loadTeacherData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                _assignedClasses.value = (doc.get("teaching_assignments") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                teacherSubject = doc.getString("subject") ?: ""
                teacherId = doc.getString("teacherId") ?: ""
            } catch (e: Exception) {
                // Silently handle or log
            }
        }
    }

    fun postAssignment(
        type: String,
        title: String,
        classSection: String,
        dueDate: Date
    ) {
        if (title.isBlank() || classSection.isBlank()) {
            _submissionState.value = AssignmentSubmissionState.Error("Please fill all fields")
            return
        }

        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _submissionState.value = AssignmentSubmissionState.Loading
            try {
                val assignmentData = hashMapOf(
                    "type" to type,
                    "title" to title,
                    "class_section" to classSection,
                    "subject" to teacherSubject,
                    "teacher_id" to teacherId,
                    "date" to Timestamp(dueDate),
                    "created_at" to Timestamp.now()
                )

                firestore.collection("assignments_and_exams")
                    .add(assignmentData)
                    .await()

                _submissionState.value = AssignmentSubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = AssignmentSubmissionState.Error(e.localizedMessage ?: "Failed to post assignment")
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = AssignmentSubmissionState.Idle
    }
}
