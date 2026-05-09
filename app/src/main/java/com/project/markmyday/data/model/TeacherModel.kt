package com.project.markmyday.data.model

data class TeacherModel(
    val uid: String = "",
    val name: String = ""
) {
    // Override toString to show only the name in the Spinner
    override fun toString(): String {
        return name
    }
}
