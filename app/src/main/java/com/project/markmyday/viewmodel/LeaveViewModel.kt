package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.markmyday.data.model.LeaveRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

sealed class LeaveSubmissionState {
    object Idle : LeaveSubmissionState()
    object Loading : LeaveSubmissionState()
    object Success : LeaveSubmissionState()
    data class Error(val message: String) : LeaveSubmissionState()
}

class LeaveViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _leaveHistory = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveHistory: StateFlow<List<LeaveRequest>> = _leaveHistory.asStateFlow()

    private val _submissionState = MutableStateFlow<LeaveSubmissionState>(LeaveSubmissionState.Idle)
    val submissionState: StateFlow<LeaveSubmissionState> = _submissionState.asStateFlow()

    private var studentClassSection: String = ""

    init {
        loadStudentData()
        fetchLeaveHistory()
    }

    private fun loadStudentData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                studentClassSection = doc.getString("class_section") ?: ""
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun fetchLeaveHistory() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("leaves")
            .whereEqualTo("student_id", uid)
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val history = snapshot.documents.map { doc ->
                        LeaveRequest(
                            id = doc.id,
                            studentId = doc.getString("student_id") ?: "",
                            classSection = doc.getString("class_section") ?: "",
                            startDate = doc.getTimestamp("start_date"),
                            endDate = doc.getTimestamp("end_date"),
                            reason = doc.getString("reason") ?: "",
                            status = doc.getString("status") ?: "pending",
                            appliedAt = doc.getTimestamp("appliedAt")
                        )
                    }
                    _leaveHistory.value = history
                }
            }
    }

    fun applyLeave(startDate: Date, endDate: Date, reason: String) {
        if (reason.isBlank()) {
            _submissionState.value = LeaveSubmissionState.Error("Please provide a reason")
            return
        }

        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _submissionState.value = LeaveSubmissionState.Loading
            try {
                val leaveData = hashMapOf(
                    "student_id" to uid,
                    "class_section" to studentClassSection,
                    "start_date" to Timestamp(startDate),
                    "end_date" to Timestamp(endDate),
                    "reason" to reason,
                    "status" to "pending",
                    "appliedAt" to Timestamp.now()
                )

                firestore.collection("leaves").add(leaveData).await()
                _submissionState.value = LeaveSubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = LeaveSubmissionState.Error(e.localizedMessage ?: "Failed to submit request")
            }
        }
    }

    fun resetSubmissionState() {
        _submissionState.value = LeaveSubmissionState.Idle
    }
}
