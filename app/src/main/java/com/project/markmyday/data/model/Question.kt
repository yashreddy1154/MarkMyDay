package com.project.markmyday.data.model

data class Question(
    val id: String = "",
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val subject: String = "",
    val className: String = "",
    val type: String = "Multiple Choice",
    val difficulty: String = "Medium",
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", emptyList(), "", "", "", "Multiple Choice", "Medium", System.currentTimeMillis())
}
