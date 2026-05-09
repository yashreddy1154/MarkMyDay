package com.project.markmyday.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.project.markmyday.data.model.VideoEngagement
import com.project.markmyday.data.model.StudentEngagementSummary
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class EngagementRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val engagementCollection = firestore.collection("student_engagement")

    suspend fun updateWatchTime(
        studentId: String,
        studentName: String,
        className: String,
        videoId: String,
        title: String,
        secondsToAdd: Long
    ) {
        if (studentId.isEmpty() || videoId.isEmpty()) return

        val studentDoc = engagementCollection.document(studentId)
        
        // Update basic student info
        studentDoc.set(
            mapOf(
                "studentName" to studentName,
                "studentId" to studentId,
                "className" to className
            ),
            SetOptions.merge()
        ).await()

        // Update video specific stats
        val videoDoc = studentDoc.collection("video_stats").document(videoId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(videoDoc)
            val currentSeconds = snapshot.getLong("timeSpentSeconds") ?: 0L
            
            val data = mapOf(
                "videoId" to videoId,
                "title" to title,
                "timeSpentSeconds" to (currentSeconds + secondsToAdd),
                "lastWatchedDate" to System.currentTimeMillis()
            )
            transaction.set(videoDoc, data, SetOptions.merge())
        }.await()
    }

    suspend fun getEngagementForClass(className: String): List<StudentEngagementSummary> {
        return try {
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val studentsSnapshot = engagementCollection
                .whereEqualTo("className", className)
                .get()
                .await()

            val summaries = mutableListOf<StudentEngagementSummary>()

            for (doc in studentsSnapshot.documents) {
                val studentId = doc.id
                val name = doc.getString("studentName") ?: ""
                val cls = doc.getString("className") ?: ""
                
                // Fetch video stats updated today
                val statsSnapshot = engagementCollection.document(studentId)
                    .collection("video_stats")
                    .whereGreaterThanOrEqualTo("lastWatchedDate", startOfToday)
                    .get()
                    .await()
                
                val statsMap = statsSnapshot.documents.associate { statDoc ->
                    val engagement = statDoc.toObject(VideoEngagement::class.java)!!
                    statDoc.id to engagement
                }
                
                if (statsMap.isNotEmpty()) {
                    summaries.add(
                        StudentEngagementSummary(
                            studentName = name,
                            studentId = studentId,
                            className = cls,
                            videoStats = statsMap
                        )
                    )
                }
            }
            summaries
        } catch (e: Exception) {
            emptyList()
        }
    }
}
