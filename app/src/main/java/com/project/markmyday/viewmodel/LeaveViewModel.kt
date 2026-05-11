package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.project.markmyday.data.model.LeaveRequest
import com.project.markmyday.data.model.NotificationData
import com.google.firebase.database.FirebaseDatabase
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

    private val realtimeDb = FirebaseDatabase.getInstance().getReference("notifications")

    private val _leaveHistory = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val leaveHistory: StateFlow<List<LeaveRequest>> = _leaveHistory.asStateFlow()

    private val _allLeaves = MutableStateFlow<List<LeaveRequest>>(emptyList())
    val allLeaves: StateFlow<List<LeaveRequest>> = _allLeaves.asStateFlow()

    private val _submissionState = MutableStateFlow<LeaveSubmissionState>(LeaveSubmissionState.Idle)
    val submissionState: StateFlow<LeaveSubmissionState> = _submissionState.asStateFlow()

    private var studentClassSection: String = ""
    private var studentName: String = ""

    private var historyListener: ListenerRegistration? = null
    private var allLeavesListener: ListenerRegistration? = null

    init {
        loadUserData()
        fetchLeaveHistory()
        fetchAllLeaves() // For Admin/Teacher
        autoDeleteOldLeaves()
    }

    private fun autoDeleteOldLeaves() {
        viewModelScope.launch {
            val thirtyDaysAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
            }.time
            
            try {
                val oldLeaves = firestore.collection("leaves")
                    .whereLessThan("appliedAt", Timestamp(thirtyDaysAgo))
                    .get()
                    .await()
                
                if (!oldLeaves.isEmpty) {
                    val batch = firestore.batch()
                    for (document in oldLeaves.documents) {
                        batch.delete(firestore.collection("leaves").document(document.id))
                    }
                    batch.commit().await()
                    android.util.Log.d("LeaveViewModel", "Auto-deleted ${oldLeaves.size()} old leave requests.")
                }
            } catch (e: Exception) {
                android.util.Log.e("LeaveViewModel", "Error auto-deleting old leaves", e)
            }
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                studentClassSection = doc.getString("class_section") ?: doc.getString("studentClass") ?: ""
                studentName = doc.getString("name") ?: "Student"
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun fetchLeaveHistory() {
        val uid = auth.currentUser?.uid ?: return
        historyListener = firestore.collection("leaves")
            .whereEqualTo("studentId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("LeaveViewModel", "Error fetching history", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val history = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(LeaveRequest::class.java)?.copy(id = doc.id)
                    }.sortedByDescending { it.appliedAt }
                    _leaveHistory.value = history
                }
            }
    }

    private fun fetchAllLeaves() {
        allLeavesListener = firestore.collection("leaves")
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null) {
                    val leaves = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(LeaveRequest::class.java)?.copy(id = doc.id)
                    }
                    _allLeaves.value = leaves
                }
            }
    }

    fun applyLeave(startDate: Date, endDate: Date?, reason: String, category: String) {
        if (reason.isBlank()) {
            _submissionState.value = LeaveSubmissionState.Error("Please provide a reason")
            return
        }

        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _submissionState.value = LeaveSubmissionState.Loading
            try {
                // If endDate is null, it's a single day leave
                val actualEndDate = endDate ?: startDate
                
                val leaveRequest = LeaveRequest(
                    studentId = uid,
                    studentName = studentName,
                    classSection = studentClassSection,
                    startDate = Timestamp(startDate),
                    endDate = Timestamp(actualEndDate),
                    reason = reason,
                    category = category,
                    status = "pending",
                    appliedAt = Timestamp.now()
                )

                firestore.collection("leaves").add(leaveRequest).await()
                _submissionState.value = LeaveSubmissionState.Success
            } catch (e: Exception) {
                _submissionState.value = LeaveSubmissionState.Error(e.localizedMessage ?: "Failed to submit request")
            }
        }
    }

    fun updateLeaveStatus(leaveRequest: LeaveRequest, status: String, reason: String = "") {
        viewModelScope.launch {
            try {
                val updateData = mutableMapOf<String, Any>(
                    "status" to status,
                    "statusUpdateTimestamp" to Timestamp.now()
                )
                if (reason.isNotEmpty()) {
                    updateData["rejectionReason"] = reason
                }
                
                firestore.collection("leaves").document(leaveRequest.id)
                    .update(updateData)
                    .await()
                
                // Add to Realtime Database notifications for the student
                sendLeaveStatusNotification(leaveRequest, status, reason)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun sendLeaveStatusNotification(leaveRequest: LeaveRequest, status: String, reason: String = "") {
        val notificationId = realtimeDb.push().key ?: return
        
        val heading = if (status == "approved") "Leave Approved! ✅" else "Leave Rejected ❌"
        var message = if (status == "approved") {
            "Your leave request for ${leaveRequest.studentName} has been approved."
        } else {
            "Your leave request for ${leaveRequest.studentName} was rejected."
        }
        
        if (reason.isNotEmpty()) {
            message += "\nReason: $reason"
        }
        
        val notification = NotificationData(
            id = notificationId,
            heading = heading,
            message = message,
            author = "Administrator",
            audience = leaveRequest.studentId, // Uses the studentId stored in the request
            timestamp = System.currentTimeMillis()
        )
        
        realtimeDb.child(notificationId).setValue(notification)
    }

    fun resetSubmissionState() {
        _submissionState.value = LeaveSubmissionState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        historyListener?.remove()
        allLeavesListener?.remove()
    }
}
