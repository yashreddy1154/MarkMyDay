package com.example.markmyday2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markmyday2.data.model.Attendance
import com.example.markmyday2.data.model.AttendanceStatus
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentViewModel(private val repository: StudentRepository = StudentRepository()) : ViewModel() {

    private val _attendanceRecords = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceRecords: StateFlow<List<Attendance>> = _attendanceRecords

    private val _attendancePercentage = MutableStateFlow(0f)
    val attendancePercentage: StateFlow<Float> = _attendancePercentage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    fun loadAttendance(studentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val records = repository.getAttendanceForStudent(studentId)
            _attendanceRecords.value = records
            
            if (records.isNotEmpty()) {
                val presentCount = records.count { it.status == AttendanceStatus.PRESENT }
                _attendancePercentage.value = (presentCount.toFloat() / records.size) * 100
            } else {
                _attendancePercentage.value = 0f
            }
            
            _isLoading.value = false
        }
    }

    fun loadTimetable(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _timetable.value = repository.getTimetableForClass(classId)
            _isLoading.value = false
        }
    }
}
