package com.project.markmyday.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.model.Question
import com.project.markmyday.data.model.QuizAttempt
import com.project.markmyday.data.model.QuizResult
import com.project.markmyday.data.repository.QuizAttemptRepository
import com.project.markmyday.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import java.util.Locale

class QuizTakingViewModel(application: Application) : AndroidViewModel(application) {
    private val quizRepository = QuizRepository()
    private val attemptRepository = QuizAttemptRepository(application)

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _currentAttempt = MutableStateFlow(QuizAttempt())
    val currentAttempt: StateFlow<QuizAttempt> = _currentAttempt

    private val _timeLeftFormatted = MutableStateFlow("00:00")
    val timeLeftFormatted: StateFlow<String> = _timeLeftFormatted

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Idle)
    val quizState: StateFlow<QuizState> = _quizState

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private var studentName: String = "Student"
    private var className: String = "N/A"
    private var subject: String = "General"

    private var timer: CountDownTimer? = null

    private val _availableQuizzes = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val availableQuizzes: StateFlow<List<Pair<String, String>>> = _availableQuizzes

    fun loadAvailableQuizzes(className: String) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            quizRepository.getAvailableSubjectsForClass(className).collect { subjects ->
                _availableQuizzes.value = subjects
                _quizState.value = QuizState.Idle
            }
        }
    }

    fun startQuiz(className: String, studentName: String = "Student", studentId: String = "") {
        this.studentName = studentName
        this.className = className
        this.subject = "Mixed"
        val actualDuration: Long = 15 * 60 * 1000 
        
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            
            // 1. Fetch mixed questions for the specific class
            val dbClass = if (!className.startsWith("Class")) "Class $className" else className
            quizRepository.getQuestionsForClass(dbClass).collect { allClassQuestions ->
                
                // 2. Identify questions already taken today (to ensure fresh ones in Attempt 2)
                val takenIds = quizRepository.getAttemptedQuestionIds(studentId, className)
                
                // Filter out already taken questions
                val freshQuestions = allClassQuestions.filterNot { it.id in takenIds }
                
                // 3. Shuffle and take 20
                val uniqueQuestions = freshQuestions.distinctBy { it.text }
                val attemptQuestions = uniqueQuestions.shuffled().take(20)
                
                // 4. Check Attempt Limit based on total results in Firestore
                val totalAttempts = quizRepository.getAttemptCountForMixed(studentId, className)
                if (totalAttempts >= 2) {
                    _quizState.value = QuizState.Error("You have already taken this test 2 times today.")
                    return@collect
                }

                _questions.value = attemptQuestions
                
                if (attemptQuestions.isNotEmpty()) {
                    val cacheKey = "${studentId}_${className}_mixed"
                    val existingAttempt = attemptRepository.getAttempt(cacheKey)
                    
                    if (existingAttempt != null && !existingAttempt.isCompleted) {
                        _currentAttempt.value = existingAttempt
                        startTimer(existingAttempt.timeLeftMillis)
                    } else {
                        _currentAttempt.value = QuizAttempt(
                            quizId = cacheKey,
                            studentId = studentId,
                            timeLeftMillis = actualDuration,
                            totalQuestions = attemptQuestions.size
                        )
                        startTimer(actualDuration)
                    }
                }
                _quizState.value = QuizState.InProgress
            }
        }
    }

    private fun startTimer(duration: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _currentAttempt.value = _currentAttempt.value.copy(timeLeftMillis = millisUntilFinished)
                updateFormattedTime(millisUntilFinished)
                // Auto-save every tick or at intervals
                attemptRepository.saveAttempt(_currentAttempt.value)
            }

            override fun onFinish() {
                submitQuiz()
            }
        }.start()
    }

    private fun updateFormattedTime(millis: Long) {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        _timeLeftFormatted.value = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun onAnswerSelected(questionId: String, answer: String) {
        val updatedAnswers = _currentAttempt.value.answers.toMutableMap()
        updatedAnswers[questionId] = answer
        _currentAttempt.value = _currentAttempt.value.copy(answers = updatedAnswers)
        attemptRepository.saveAttempt(_currentAttempt.value)
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.size - 1) {
            _currentQuestionIndex.value += 1
        } else {
            submitQuiz()
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun onAppPaused() {
        val updatedAttempt = _currentAttempt.value.copy(
            warningCount = _currentAttempt.value.warningCount + 1
        )
        _currentAttempt.value = updatedAttempt
        attemptRepository.saveAttempt(updatedAttempt)
        _quizState.value = QuizState.Warning("Please don't switch apps during the test!")
    }

    fun submitQuiz() {
        timer?.cancel()
        val score = calculateScore()
        _currentAttempt.value = _currentAttempt.value.copy(
            isCompleted = true,
            score = score
        )
        attemptRepository.saveAttempt(_currentAttempt.value)
        
        // Save to Firestore for Leaderboard
        viewModelScope.launch {
            val result = QuizResult(
                studentId = _currentAttempt.value.studentId,
                studentName = studentName,
                score = score,
                totalQuestions = _questions.value.size,
                timeTakenMillis = 900000 - _currentAttempt.value.timeLeftMillis, // 15 mins base
                subject = subject,
                className = className,
                attemptedQuestionIds = _questions.value.map { it.id }
            )
            quizRepository.saveQuizResult(result)
        }

        _quizState.value = QuizState.Finished(score, _questions.value.size)
    }

    private fun calculateScore(): Int {
        var score = 0
        val answers = _currentAttempt.value.answers
        _questions.value.forEach { question ->
            if (answers[question.id] == question.correctAnswer) {
                score++
            }
        }
        return score
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}

sealed class QuizState {
    object Idle : QuizState()
    object Loading : QuizState()
    object InProgress : QuizState()
    data class Warning(val message: String) : QuizState()
    data class Error(val message: String) : QuizState()
    data class Finished(val score: Int, val total: Int) : QuizState()
}
