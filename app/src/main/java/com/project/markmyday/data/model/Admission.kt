package com.project.markmyday.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Admission(
    val id: String = "",
    val name: String = "",
    val age: String = "",
    val className: String = "",
    val parentName: String = "",
    val phone: String = "",
    val addedBy: String = "", // Role of the person who added it
    val addedByName: String = "", // Name of the person who added it
    @ServerTimestamp val timestamp: Date? = null
)
