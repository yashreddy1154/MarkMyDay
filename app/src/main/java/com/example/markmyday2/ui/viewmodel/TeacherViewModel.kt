package com.example.markmyday2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markmyday2.data.model.Attendance
import com.example.markmyday2.data.model.AttendanceStatus
import com.example.markmyday2.data.model.ClassInfo
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.data.model.User
import com.example.markmyday2.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherViewModel(private val repository: TeacherRepository = TeacherRepository()) : ViewModel() {

    private val _classes = MutableStateFlow<List<ClassInfo>>(emptyList())
    val classes: StateFlow<List<ClassInfo>> = _classes

    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    private val _submissionStatus = MutableStateFlow<Result<Unit>?>(null)
    val submissionStatus: StateFlow<Result<Unit>?> = _submissionStatus

    fun loadAssignedClasses(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _classes.value = repository.getAssignedClasses(teacherId)
            _isLoading.value = false
        }
    }

    fun loadStudents(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _students.value = repository.getStudentsByClass(classId)
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

    fun submitAttendance(classId: String, teacherId: String, attendanceMap: Map<String, AttendanceStatus>) {
        viewModelScope.launch {
            _isLoading.value = true
            _submissionStatus.value = null
            try {
                val attendanceList = attendanceMap.map { (studentId, status) ->
                    val studentName = _students.value.find { it.userId == studentId }?.name ?: ""
                    Attendance(
                        studentId = studentId,
                        studentName = studentName,
                        status = status,
                        classId = classId,
                        teacherId = teacherId,
                        date = System.currentTimeMillis()
                    )
                }
                repository.saveAttendance(attendanceList)
                _submissionStatus.value = Result.success(Unit)
            } catch (e: Exception) {
                _submissionStatus.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSubmissionStatus() {
        _submissionStatus.value = null
    }
}
