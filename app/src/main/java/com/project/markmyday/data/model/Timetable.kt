package com.project.markmyday.data.model

data class Period(
    val periodNumber: Int = 0,
    val startTime: String = "",
    val endTime: String = "",
    val subject: String = "",
    val teacherId: String = "",
    val teacherName: String = ""
)

data class DaySchedule(
    val periods: List<Period> = emptyList()
)

data class SubjectQuota(
    val subject: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val classCount: Int = 0
)

data class Timetable(
    val className: String = "", // e.g., "Class 1"
    val homeTeacherId: String = "",
    val homeTeacherName: String = "",
    val studentList: List<String> = emptyList(),
    val weeklySchedule: Map<String, DaySchedule> = emptyMap(), // Key: "Monday", "Tuesday", etc.
    val weeklyQuota: Map<String, SubjectQuota> = emptyMap(), // Key: Subject Name
    val totalWeeklyClasses: Int = 0
)
