package com.example.markmyday2.data.repository

import com.example.markmyday2.data.model.Attendance
import com.example.markmyday2.data.model.TimetableEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getAttendanceForStudent(studentId: String): List<Attendance> {
        return try {
            firestore.collection("attendance")
                .whereEqualTo("studentId", studentId)
                .get().await().toObjects(Attendance::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTimetableForClass(classId: String): List<TimetableEntry> {
        return try {
            firestore.collection("timetable")
                .whereEqualTo("classId", classId)
                .get()
                .await()
                .toObjects(TimetableEntry::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
