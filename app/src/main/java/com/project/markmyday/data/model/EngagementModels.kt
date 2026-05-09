package com.project.markmyday.data.model

data class VideoEngagement(
    val videoId: String = "",
    val title: String = "",
    val timeSpentSeconds: Long = 0,
    val lastWatchedDate: Long = System.currentTimeMillis()
)

data class StudentEngagementSummary(
    val studentName: String = "",
    val studentId: String = "",
    val className: String = "",
    val videoStats: Map<String, VideoEngagement> = emptyMap()
)
