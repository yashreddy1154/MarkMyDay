package com.project.markmyday.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.CourseVideo
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CourseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val courseCollection = firestore.collection("video_courses")

    fun getCoursesForClass(classLevel: String): Flow<List<CourseVideo>> = flow {
        try {
            val baseClass = classLevel.lowercase().replace("class ", "").trim()
            val variations = listOf(
                classLevel,
                classLevel.trim(),
                baseClass,
                "Class $baseClass",
                "class $baseClass",
                "$baseClass ",
                "Class $baseClass ",
                classLevel.lowercase(),
                classLevel.uppercase()
            ).distinct()

            android.util.Log.d("CourseRepo", "Querying video_courses with variations: $variations")

            val snapshot = courseCollection
                .whereIn("class_level", variations)
                .get()
                .await()
            val courses = snapshot.toObjects(CourseVideo::class.java)
            android.util.Log.d("CourseRepo", "Firestore returned ${courses.size} documents")
            emit(courses)
        } catch (e: Exception) {
            android.util.Log.e("CourseRepo", "Firestore query failed", e)
            emit(emptyList())
        }
    }

    suspend fun uploadVideos(videos: List<CourseVideo>): Result<Int> {
        return try {
            val batch = firestore.batch()
            videos.forEach { video ->
                val docRef = courseCollection.document()
                batch.set(docRef, video)
            }
            batch.commit().await()
            Result.success(videos.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCourse(video: CourseVideo): Result<Unit> {
        return try {
            courseCollection.document().set(video).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
