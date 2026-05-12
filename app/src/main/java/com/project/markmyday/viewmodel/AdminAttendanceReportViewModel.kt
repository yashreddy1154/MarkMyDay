package com.project.markmyday.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.utils.ExcelUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

sealed class ReportGenerationState {
    object Idle : ReportGenerationState()
    object Loading : ReportGenerationState()
    object Success : ReportGenerationState()
    data class Error(val message: String) : ReportGenerationState()
}

class AdminAttendanceReportViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _reportState = MutableStateFlow<ReportGenerationState>(ReportGenerationState.Idle)
    val reportState: StateFlow<ReportGenerationState> = _reportState.asStateFlow()

    fun generateReport(context: Context) {
        viewModelScope.launch {
            _reportState.value = ReportGenerationState.Loading
            try {
                // 1. Fetch all attendance records
                val attendanceSnapshot = firestore.collection("attendance")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()

                val records = mutableListOf<ExcelUtils.StudentAttendanceRecord>()
                
                // Collect all student UIDs to fetch names in bulk if possible, 
                // but for simplicity and reliability with smaller sets, we'll map them.
                // In a production app, we'd cache student info or use a better schema.
                
                val studentCache = mutableMapOf<String, Pair<String, String>>() // UID -> Pair(Name, ID)

                for (doc in attendanceSnapshot.documents) {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(doc.getTimestamp("date")?.toDate() ?: Date())
                    val className = doc.getString("class_section") ?: "Unknown"
                    val subject = doc.getString("subject") ?: "General"
                    
                    val presentUids = doc.get("present_students") as? List<String> ?: emptyList()
                    val absentUids = doc.get("absent_students") as? List<String> ?: emptyList()

                    // Fetch student details for present students
                    for (uid in presentUids) {
                        val studentInfo = getStudentInfo(uid, studentCache)
                        records.add(ExcelUtils.StudentAttendanceRecord(
                            studentName = studentInfo.first,
                            studentId = studentInfo.second,
                            className = className,
                            date = dateStr,
                            status = "Present",
                            subject = subject
                        ))
                    }

                    // Fetch student details for absent students
                    for (uid in absentUids) {
                        val studentInfo = getStudentInfo(uid, studentCache)
                        records.add(ExcelUtils.StudentAttendanceRecord(
                            studentName = studentInfo.first,
                            studentId = studentInfo.second,
                            className = className,
                            date = dateStr,
                            status = "Absent",
                            subject = subject
                        ))
                    }
                }

                if (records.isEmpty()) {
                    _reportState.value = ReportGenerationState.Error("No attendance records found to export.")
                } else {
                    ExcelUtils.exportStudentAttendanceToExcel(context, records)
                    _reportState.value = ReportGenerationState.Success
                }

            } catch (e: Exception) {
                _reportState.value = ReportGenerationState.Error(e.localizedMessage ?: "Failed to generate report")
            }
        }
    }

    private suspend fun getStudentInfo(uid: String, cache: MutableMap<String, Pair<String, String>>): Pair<String, String> {
        if (cache.containsKey(uid)) return cache[uid]!!
        
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            val name = userDoc.getString("name") ?: "Unknown"
            val studentId = userDoc.getString("studentId") ?: "N/A"
            val info = Pair(name, studentId)
            cache[uid] = info
            info
        } catch (e: Exception) {
            Pair("Unknown ($uid)", "N/A")
        }
    }

    fun resetState() {
        _reportState.value = ReportGenerationState.Idle
    }
}
