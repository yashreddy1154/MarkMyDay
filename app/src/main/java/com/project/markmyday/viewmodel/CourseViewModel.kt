package com.project.markmyday.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.model.CourseVideo
import com.project.markmyday.data.repository.CourseRepository
import com.project.markmyday.utils.FileParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel : ViewModel() {
    private val repository = CourseRepository()

    private val _uploadStatus = MutableStateFlow<CourseUploadStatus>(CourseUploadStatus.Idle)
    val uploadStatus: StateFlow<CourseUploadStatus> = _uploadStatus

    private val _coursesBySubject = MutableStateFlow<Map<String, List<CourseVideo>>>(emptyMap())
    val coursesBySubject: StateFlow<Map<String, List<CourseVideo>>> = _coursesBySubject.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchAllCourses() {
        android.util.Log.d("CourseViewModel", "Fetching all courses (Global)")
        viewModelScope.launch {
            _isLoading.value = true
            repository.getAllCourses().collect { courses ->
                android.util.Log.d("CourseViewModel", "Found ${courses.size} global courses")
                _coursesBySubject.value = courses.groupBy { it.subject }
                _isLoading.value = false
            }
        }
    }

    fun uploadCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = CourseUploadStatus.Loading
            val videos = FileParser.parseVideoCsv(context, uri)
            if (videos.isEmpty()) {
                _uploadStatus.value = CourseUploadStatus.Error("No valid videos found or invalid format")
                return@launch
            }
            
            val result = repository.uploadVideos(videos)
            if (result.isSuccess) {
                _uploadStatus.value = CourseUploadStatus.Success(result.getOrNull() ?: 0)
            } else {
                _uploadStatus.value = CourseUploadStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun saveManualCourse(video: CourseVideo) {
        viewModelScope.launch {
            _uploadStatus.value = CourseUploadStatus.Loading
            val result = repository.addCourse(video)
            if (result.isSuccess) {
                _uploadStatus.value = CourseUploadStatus.Success(1)
            } else {
                _uploadStatus.value = CourseUploadStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetStatus() {
        _uploadStatus.value = CourseUploadStatus.Idle
    }
}

sealed class CourseUploadStatus {
    object Idle : CourseUploadStatus()
    object Loading : CourseUploadStatus()
    data class Success(val count: Int) : CourseUploadStatus()
    data class Error(val message: String) : CourseUploadStatus()
}
