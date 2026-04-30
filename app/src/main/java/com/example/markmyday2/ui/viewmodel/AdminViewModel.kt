package com.example.markmyday2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markmyday2.data.model.ClassInfo
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.data.model.User
import com.example.markmyday2.data.model.UserRole
import com.example.markmyday2.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: AdminRepository = AdminRepository()) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _classes = MutableStateFlow<List<ClassInfo>>(emptyList())
    val classes: StateFlow<List<ClassInfo>> = _classes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _users.value = repository.getAllUsers()
                _classes.value = repository.getAllClasses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun addTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addTimetableEntry(entry)
            if (result.isFailure) {
                _errorMessage.value = "Failed to add timetable: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun fetchTimetableForClass(classId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _timetable.value = repository.getTimetableForClass(classId)
            _isLoading.value = false
        }
    }

    fun createUser(name: String, email: String, role: UserRole, classId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val newUser = User(
                userId = java.util.UUID.randomUUID().toString(),
                name = name,
                email = email,
                role = role,
                classId = classId
            )
            val result = repository.createTeacher(newUser)
            if (result.isSuccess) {
                loadData()
            } else {
                _errorMessage.value = "Failed to create user: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            loadData()
        }
    }

    fun createClass(className: String, teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val classInfo = ClassInfo(className = className, teacherId = teacherId)
            repository.createClass(classInfo)
            loadData()
            _isLoading.value = false
        }
    }

    fun deleteClass(classId: String) {
        viewModelScope.launch {
            repository.deleteClass(classId)
            loadData()
        }
    }

    fun seedClasses() {
        viewModelScope.launch {
            for (i in 1..10) {
                repository.createClass(ClassInfo(className = "Class $i", teacherId = "system_default"))
            }
            loadData()
        }
    }
}
