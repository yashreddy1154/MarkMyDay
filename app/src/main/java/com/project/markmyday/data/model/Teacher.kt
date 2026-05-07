package com.project.markmyday.data.model

data class Teacher(
    val teacherId: String = "",
    val name: String = "",
    val age: Int = 0,
    val dob: String = "", // format dd/MM/yyyy or ddmmyyyy
    val gender: String = "",
    val subject: String = "",
    val homeSection: String = "",
    val classesTaught: List<String> = emptyList(),
    val classesTaughtCategories: List<String> = emptyList(), // Primary, Secondary, High School
    val phone: String = "",
    val email: String = ""
)
