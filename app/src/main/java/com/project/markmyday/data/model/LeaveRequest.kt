package com.project.markmyday.data.model

import com.google.firebase.Timestamp

data class LeaveRequest(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val classSection: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val reason: String = "",
    val category: String = "Personal",
    val status: String = "pending",
    val appliedAt: Timestamp? = null,
    val statusUpdateTimestamp: Timestamp? = null,
    val rejectionReason: String = "",
)
