package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Filter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SubjectAttendance(
    val subject: String,
    val attended: Int,
    val total: Int,
    val percentage: Float
)

class StudentAttendanceViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _attendanceStats = MutableStateFlow<List<SubjectAttendance>>(emptyList())
    val attendanceStats: StateFlow<List<SubjectAttendance>> = _attendanceStats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAttendanceStats()
    }

    private fun fetchAttendanceStats() {
        val uid = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Query for all attendance records where student is either present or absent
                val query = firestore.collection("attendance")
                    .where(
                        Filter.or(
                            Filter.arrayContains("present_students", uid),
                            Filter.arrayContains("absent_students", uid)
                        )
                    )
                
                val snapshot = query.get().await()
                
                val subjectsMap = mutableMapOf<String, Pair<Int, Int>>() // Subject -> Pair(Attended, Total)
                
                for (doc in snapshot.documents) {
                    val subject = doc.getString("subject") ?: "Unknown"
                    val presentList = (doc.get("present_students") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    
                    val isPresent = presentList.contains(uid)
                    
                    val currentStats = subjectsMap.getOrDefault(subject, Pair(0, 0))
                    subjectsMap[subject] = Pair(
                        if (isPresent) currentStats.first + 1 else currentStats.first,
                        currentStats.second + 1
                    )
                }
                
                val statsList = subjectsMap.map { (subject, stats) ->
                    val attended = stats.first
                    val total = stats.second
                    SubjectAttendance(
                        subject = subject,
                        attended = attended,
                        total = total,
                        percentage = if (total > 0) (attended.toFloat() / total) else 0f
                    )
                }.sortedBy { it.subject }
                
                _attendanceStats.value = statsList
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
