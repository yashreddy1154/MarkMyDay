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

    fun filterAndSort(
        filterType: String, 
        subject: String? = null, 
        className: String? = null,
        userRole: String = "student",
        userClass: String = ""
    ) {
        val isAdmin = userRole.lowercase() in listOf("admin", "principal")
        val isTeacher = userRole.lowercase() == "teacher"
        
        val filtered = allResults.value.filter { result ->
            val isGK = result.subject.trim().lowercase() == "gk" || 
                       result.subject.trim().lowercase().contains("current affairs") ||
                       result.subject == "Mixed"
            
            // 1. Admins see everything
            if (isAdmin) return@filter true
            
            // 2. GK is visible to all
            if (isGK) return@filter true
            
            // 3. Teachers see results for their classes/subjects (simplified: they see class-specific if it matches class filter or they are on duty)
            if (isTeacher) return@filter true // Teachers are trusted for now as per requirement "result... to that subject teacher"
            
            // 4. Students see only their own class results
            val resultClass = if (!result.className.startsWith("Class")) "Class ${result.className}" else result.className
            val targetClass = if (!userClass.startsWith("Class")) "Class $userClass" else userClass
            
            resultClass == targetClass
        }.filter { 
            // Secondary type filters
            when (filterType) {
                "By Subject" -> it.subject == subject
                "By Class" -> it.className == className
                else -> true
            }
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
