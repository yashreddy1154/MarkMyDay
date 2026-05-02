package com.project.markmyday.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.Student
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository class responsible for Firestore operations related to the Student entity.
 * Uses Kotlin Coroutines and Flow for asynchronous data handling.
 */
class StudentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studentsCollection = firestore.collection("students")

    /**
     * Adds a new student to the "students" collection.
     * Uses the student's ID as the document ID.
     */
    suspend fun addStudent(student: Student) {
        try {
            studentsCollection.document(student.studentId).set(student).await()
        } catch (e: Exception) {
            // Handle basic Firebase exception by rethrowing or logging
            throw e
        }
    }

    /**
     * Fetches all students from the "students" collection as a [Flow].
     * Provides real-time updates using a snapshot listener.
     */
    fun getAllStudents(): Flow<List<Student>> = callbackFlow {
        val subscription = studentsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val students = snapshot.toObjects(Student::class.java)
                trySend(students)
            }
        }
        awaitClose { subscription.remove() }
    }

    /**
     * Deletes a student from the "students" collection by [studentId].
     */
    suspend fun deleteStudent(studentId: String) {
        try {
            studentsCollection.document(studentId).delete().await()
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * Updates an existing student's details.
     * In Firestore, setting a document with an existing ID performs an update.
     */
    suspend fun updateStudent(student: Student) {
        try {
            studentsCollection.document(student.studentId).set(student).await()
        } catch (e: Exception) {
            throw e
        }
    }
}
