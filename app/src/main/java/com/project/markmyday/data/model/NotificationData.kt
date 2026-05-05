package com.project.markmyday.data.model

data class NotificationData(
    val id: String = "",
    val heading: String = "",
    val message: String = "",
    val author: String = "",
    val audience: String = "",
    val timestamp: Long = 0L
)
