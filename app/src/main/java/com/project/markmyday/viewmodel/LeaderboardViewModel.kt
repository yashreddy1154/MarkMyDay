package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.model.QuizResult
import com.project.markmyday.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val quizRepository = QuizRepository()
    private val allResults = MutableStateFlow<List<QuizResult>>(emptyList())
    
    private val _filteredResults = MutableStateFlow<List<QuizResult>>(emptyList())
    val filteredResults: StateFlow<List<QuizResult>> = _filteredResults

    init {
        fetchRealResults()
    }

    private fun fetchRealResults() {
        viewModelScope.launch {
            quizRepository.getAllResults().collect { results ->
                allResults.value = results
                filterAndSort("Overall")
            }
        }
    }

    fun filterAndSort(filterType: String, subject: String? = null, className: String? = null) {
        val filtered = when (filterType) {
            "By Subject" -> allResults.value.filter { it.subject == subject }
            "By Class" -> allResults.value.filter { it.className == className }
            else -> allResults.value
        }

        // Sorting Logic: 
        // 1. Descending by Score
        // 2. Ascending by Time Taken (Tie-breaker)
        val sorted = filtered.sortedWith(
            compareByDescending<QuizResult> { it.score }
                .thenBy { it.timeTakenMillis }
        )

        _filteredResults.value = sorted
    }

    fun clearLeaderboard() {
        viewModelScope.launch {
            quizRepository.clearAllResults()
            allResults.value = emptyList()
            _filteredResults.value = emptyList()
        }
    }
}
