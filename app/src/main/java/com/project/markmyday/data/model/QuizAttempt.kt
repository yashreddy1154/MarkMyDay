package com.project.markmyday.data.model

data class QuizAttempt(
    val quizId: String = "",
    val studentId: String = "",
    val answers: Map<String, String> = emptyMap(), // QuestionId -> SelectedOption
    val timeLeftMillis: Long = 0,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val warningCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", emptyMap(), 0, false, 0, 0, 0, System.currentTimeMillis())
}
