package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.model.DigitalDiaryEntry
import com.project.markmyday.data.repository.DigitalDiaryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class DiaryState {
    object Idle : DiaryState()
    object Loading : DiaryState()
    object Success : DiaryState()
    data class Error(val message: String) : DiaryState()
}

class DigitalDiaryViewModel(
    private val repository: DigitalDiaryRepository = DigitalDiaryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<DiaryState>(DiaryState.Idle)
    val state: StateFlow<DiaryState> = _state.asStateFlow()

    private val _diaryEntries = MutableStateFlow<List<DigitalDiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DigitalDiaryEntry>> = _diaryEntries.asStateFlow()

    fun postEntry(
        className: String,
        subject: String,
        teacherId: String,
        teacherName: String,
        note: String,
        homework: String
    ) {
        if (note.isBlank() && homework.isBlank()) {
            _state.value = DiaryState.Error("Please fill at least one field")
            return
        }

        viewModelScope.launch {
            _state.value = DiaryState.Loading
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val entry = DigitalDiaryEntry(
                    className = className,
                    subject = subject,
                    teacherId = teacherId,
                    teacherName = teacherName,
                    note = note,
                    homework = homework,
                    timestamp = System.currentTimeMillis(),
                    dateString = sdf.format(Date())
                )
                repository.postDiaryEntry(entry)
                _state.value = DiaryState.Success
            } catch (e: Exception) {
                _state.value = DiaryState.Error(e.localizedMessage ?: "Failed to post entry")
            }
        }
    }

    fun fetchEntriesForClass(className: String) {
        viewModelScope.launch {
            repository.getDiaryEntriesForClass(className).collect { entries ->
                _diaryEntries.value = entries
            }
        }
    }

    // Helper to get only latest entry per subject
    val latestHomeworkBySubject: StateFlow<Map<String, DigitalDiaryEntry>> = _diaryEntries.map { entries ->
        entries.filter { it.homework.isNotBlank() }
            .groupBy { it.subject }
            .mapValues { it.value.first() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun resetState() {
        _state.value = DiaryState.Idle
    }
}
