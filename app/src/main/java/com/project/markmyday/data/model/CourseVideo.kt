package com.project.markmyday.data.model

import com.google.firebase.firestore.Exclude

data class CourseVideo(
    @get:Exclude val id: String = "",
    val video_id: String = "",
    val subject: String = "",
    val class_level: String = "",
    val title: String = ""
)
