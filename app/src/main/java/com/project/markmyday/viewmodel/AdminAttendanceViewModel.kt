package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class TeacherAttendanceStatus(
    val teacher: Teacher,
    val isPresent: Boolean,
    val scanTime: String? = null
)

class AdminAttendanceViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _presentTeachers = MutableStateFlow<List<TeacherAttendanceStatus>>(emptyList())
    val presentTeachers: StateFlow<List<TeacherAttendanceStatus>> = _presentTeachers.asStateFlow()

    private val _absentTeachers = MutableStateFlow<List<TeacherAttendanceStatus>>(emptyList())
    val absentTeachers: StateFlow<List<TeacherAttendanceStatus>> = _absentTeachers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var teachersListener: com.google.firebase.firestore.ListenerRegistration? = null
    private val attendanceListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

    fun fetchDailyOverview() {
        if (teachersListener != null) return // Already listening

        _isLoading.value = true
        
        // 1. Listen for teachers collection changes
        teachersListener = firestore.collection("teachers").addSnapshotListener { teachersSnapshot, error ->
            if (error != null) {
                _isLoading.value = false
                return@addSnapshotListener
            }

            if (teachersSnapshot != null) {
                val allTeachers = teachersSnapshot.toObjects(Teacher::class.java)
                setupAttendanceListeners(allTeachers)
            }
        }
    }

    private fun setupAttendanceListeners(allTeachers: List<Teacher>) {
        // Clear old listeners
        attendanceListeners.forEach { it.remove() }
        attendanceListeners.clear()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Local map to track attendance status per teacher
        val attendanceMap = mutableMapOf<String, TeacherAttendanceStatus>()
        allTeachers.forEach { teacher ->
            attendanceMap[teacher.teacherId] = TeacherAttendanceStatus(teacher, false)
        }

        allTeachers.forEach { teacher ->
            val listener = firestore.collection("teachers")
                .document(teacher.teacherId)
                .collection("attendance_logs")
                .document(today)
                .addSnapshotListener { logDoc, error ->
                    if (error != null) return@addSnapshotListener

                    if (logDoc != null && logDoc.exists() && logDoc.getString("status") == "Present") {
                        val timestamp = logDoc.getTimestamp("time")
                        val scanTime = if (timestamp != null) {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp.toDate())
                        } else null
                        attendanceMap[teacher.teacherId] = TeacherAttendanceStatus(teacher, true, scanTime)
                    } else {
                        attendanceMap[teacher.teacherId] = TeacherAttendanceStatus(teacher, false)
                    }

                    // Update UI flows
                    val presentList = attendanceMap.values.filter { it.isPresent }.sortedByDescending { it.scanTime }
                    val absentList = attendanceMap.values.filter { !it.isPresent }.sortedBy { it.teacher.name }
                    
                    _presentTeachers.value = presentList
                    _absentTeachers.value = absentList
                    _isLoading.value = false
                }
            attendanceListeners.add(listener)
        }
    }

    override fun onCleared() {
        super.onCleared()
        teachersListener?.remove()
        attendanceListeners.forEach { it.remove() }
    }
}
