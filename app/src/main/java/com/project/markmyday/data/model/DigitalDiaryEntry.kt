package com.project.markmyday.data.model

data class DigitalDiaryEntry(
    val id: String = "",
    val className: String = "",
    val subject: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val note: String = "",
    val homework: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = "" // format: yyyy-MM-dd
)
