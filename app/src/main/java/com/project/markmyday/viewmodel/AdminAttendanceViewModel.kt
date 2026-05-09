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

    fun fetchDailyOverview() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val teachersSnapshot = firestore.collection("teachers").get().await()
                val allTeachers = teachersSnapshot.toObjects(Teacher::class.java)
                
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                
                val presentList = mutableListOf<TeacherAttendanceStatus>()
                val absentList = mutableListOf<TeacherAttendanceStatus>()

                for (teacher in allTeachers) {
                    val logDoc = firestore.collection("teachers")
                        .document(teacher.teacherId)
                        .collection("attendance_logs")
                        .document(today)
                        .get()
                        .await()

                    if (logDoc.exists() && logDoc.getString("status") == "Present") {
                        val timestamp = logDoc.getTimestamp("time")
                        val scanTime = if (timestamp != null) {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp.toDate())
                        } else null
                        presentList.add(TeacherAttendanceStatus(teacher, true, scanTime))
                    } else {
                        absentList.add(TeacherAttendanceStatus(teacher, false))
                    }
                }

                _presentTeachers.value = presentList
                _absentTeachers.value = absentList
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
