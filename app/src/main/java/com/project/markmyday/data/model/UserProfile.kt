package com.project.markmyday.data.model

data class UserProfile(
    val name: String = "",
    val role: String = "",
    val id: String = "",
    val studentClass: String? = null,
    val parentName: String? = null,
    val email: String? = null
)
