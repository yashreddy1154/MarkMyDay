package com.example.markmyday2.data.model

import com.google.firebase.firestore.PropertyName

enum class UserRole {
    ADMIN, TEACHER, STUDENT
}

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STUDENT,
    val classId: String? = null, // For students
    val subjects: List<String>? = null // For teachers
)

data class ClassInfo(
    val classId: String = "",
    val className: String = "",
    val teacherId: String = ""
)

data class Attendance(
    val attendanceId: String = "",
    val date: Long = System.currentTimeMillis(),
    val studentId: String = "",
    val studentName: String = "",
    val status: AttendanceStatus = AttendanceStatus.ABSENT,
    val classId: String = "",
    val teacherId: String = ""
)

enum class AttendanceStatus {
    PRESENT, ABSENT
}

data class TimetableEntry(
    val day: String = "", // Monday, Tuesday, etc.
    val startTime: String = "",
    val endTime: String = "",
    val subject: String = "",
    val teacherName: String = "",
    val classId: String = ""
)
