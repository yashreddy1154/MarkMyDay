package com.project.markmyday.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Teacher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TeacherRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val teachersCollection = firestore.collection("teachers")

    suspend fun addTeacher(teacher: Teacher) {
        try {
            teachersCollection.document(teacher.teacherId).set(teacher).await()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getAllTeachers(): Flow<List<Teacher>> = callbackFlow {
        val subscription = teachersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val teachers = snapshot.toObjects(Teacher::class.java)
                trySend(teachers)
            }
        }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteTeacher(teacherId: String) {
        try {
            teachersCollection.document(teacherId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateTeacher(teacher: Teacher) {
        // In Firestore, set() with an existing document ID updates the document.
        addTeacher(teacher)
    }
}
