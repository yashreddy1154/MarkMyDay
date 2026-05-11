package com.project.markmyday.data.model

data class UserProfile(
    val name: String = "",
    val role: String = "",
    val id: String = "",
    val studentClass: String? = null,
    val parentName: String? = null,
    val fatherName: String? = null,
    val motherName: String? = null,
    val fatherPhone: String? = null,
    val motherPhone: String? = null,
    val subject: String? = null,
    val email: String? = null,
    val dob: String? = null,
    val password: String? = null
)
