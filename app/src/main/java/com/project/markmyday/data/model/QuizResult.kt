package com.project.markmyday.data.model

data class QuizResult(
    val studentId: String = "",
    val studentName: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val timeTakenMillis: Long = 0,
    val subject: String = "",
    val className: String = "",
    val isPrivacyEnabled: Boolean = false,
    val attemptedQuestionIds: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", 0, 0, 0, "", "", false, emptyList(), System.currentTimeMillis())
}
