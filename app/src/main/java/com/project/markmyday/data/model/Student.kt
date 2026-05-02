package com.project.markmyday.data.model

/**
 * Data class representing a Student entity in the MarkMyDay application.
 *
 * @property studentId Unique identifier for the student.
 * @property name Full name of the student.
 * @property age Age of the student.
 * @property dob Date of birth in ddmmyyyy format.
 * @property parentName Name of the student's parent or guardian.
 * @property phone Contact phone number.
 * @property studentClass The class/grade the student belongs to.
 * @property section The section within the class.
 * @property email Email address for the student or parent.
 */
data class Student(
    val studentId: String = "",
    val name: String = "",
    val age: Int = 0,
    val dob: String = "",
    val parentName: String = "",
    val phone: String = "",
    val studentClass: String = "",
    val section: String = "",
    val email: String = ""
)
