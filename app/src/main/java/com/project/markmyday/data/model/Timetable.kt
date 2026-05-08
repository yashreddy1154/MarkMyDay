package com.project.markmyday.data.model

data class Timetable(
    val className: String = "", // e.g., "Class 1"
    val homeTeacherId: String = "",
    val homeTeacherName: String = "",
    val studentList: List<String> = emptyList()
)
