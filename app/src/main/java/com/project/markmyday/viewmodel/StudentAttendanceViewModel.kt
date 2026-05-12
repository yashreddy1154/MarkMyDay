package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.markmyday.data.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class AttendanceRecord(
    val date: String,
    val isPresent: Boolean,
    val subject: String,
    val timestamp: Timestamp
)

data class StudentAttendanceSummary(
    val studentName: String = "",
    val studentClass: String = "",
    val parentName: String = "",
    val phone: String = "",
    val overallAttendance: Float = 0f,
    val leavesTaken: Int = 0,
    val totalWorkingDays: Int = 0,
    val recentLogs: List<AttendanceRecord> = emptyList()
)

class StudentAttendanceViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _summary = MutableStateFlow(StudentAttendanceSummary())
    val summary: StateFlow<StudentAttendanceSummary> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchStudentAttendance(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Fetch Student Details
                val userDoc = firestore.collection("users").document(uid).get().await()
                val name = userDoc.getString("name") ?: "Student"
                val studentClass = userDoc.getString("studentClass") ?: "N/A"
                val motherName = userDoc.getString("motherName") ?: ""
                val fatherName = userDoc.getString("fatherName") ?: ""
                val motherPhone = userDoc.getString("motherPhone") ?: ""
                val fatherPhone = userDoc.getString("fatherPhone") ?: ""
                
                val parentName = if (motherName.isNotBlank()) motherName else fatherName
                val phone = if (motherPhone.isNotBlank()) motherPhone else fatherPhone

                // 2. Fetch Attendance Logs (Last 30 days)
                val attendanceSnapshot = firestore.collection("attendance")
                    .whereArrayContainsAny("present_students", listOf(uid))
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val absentSnapshot = firestore.collection("attendance")
                    .whereArrayContainsAny("absent_students", listOf(uid))
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val presentLogs = attendanceSnapshot.documents.map { doc ->
                    AttendanceRecord(
                        date = formatDate(doc.getTimestamp("date")),
                        isPresent = true,
                        subject = doc.getString("subject") ?: "General",
                        timestamp = doc.getTimestamp("date") ?: Timestamp.now()
                    )
                }

                val absentLogs = absentSnapshot.documents.map { doc ->
                    AttendanceRecord(
                        date = formatDate(doc.getTimestamp("date")),
                        isPresent = false,
                        subject = doc.getString("subject") ?: "General",
                        timestamp = doc.getTimestamp("date") ?: Timestamp.now()
                    )
                }

                val allLogs = (presentLogs + absentLogs).sortedByDescending { it.timestamp.seconds }

                // 3. Fetch Leaves Taken
                val leavesSnapshot = firestore.collection("leaves")
                    .whereEqualTo("studentId", uid)
                    .whereEqualTo("status", "approved")
                    .get()
                    .await()
                
                val leavesCount = leavesSnapshot.size()

                // 4. Calculate Percentage
                val totalDays = allLogs.size
                val presentDays = allLogs.count { it.isPresent }
                val percentage = if (totalDays > 0) (presentDays.toFloat() / totalDays) * 100 else 0f

                _summary.value = StudentAttendanceSummary(
                    studentName = name,
                    studentClass = studentClass,
                    parentName = parentName,
                    phone = phone,
                    overallAttendance = percentage,
                    leavesTaken = leavesCount,
                    totalWorkingDays = totalDays,
                    recentLogs = allLogs.take(10)
                )

            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDate(timestamp: Timestamp?): String {
        if (timestamp == null) return "N/A"
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}
