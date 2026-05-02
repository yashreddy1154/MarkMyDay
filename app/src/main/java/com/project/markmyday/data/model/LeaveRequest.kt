package com.project.markmyday.data.model

import com.google.firebase.Timestamp

data class LeaveRequest(
    val id: String = "",
    val studentId: String = "",
    val classSection: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val reason: String = "",
    val status: String = "pending",
    val appliedAt: Timestamp? = null
)
