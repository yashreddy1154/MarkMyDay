package com.example.markmyday2.data.repository

import com.example.markmyday2.data.model.Attendance
import com.example.markmyday2.data.model.ClassInfo
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TeacherRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getAssignedClasses(teacherId: String): List<ClassInfo> {
        return try {
            firestore.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .await()
                .toObjects(ClassInfo::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getStudentsByClass(classId: String): List<User> {
        return try {
            firestore.collection("users")
                .whereEqualTo("role", "STUDENT")
                .whereEqualTo("classId", classId)
                .get()
                .await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAttendance(records: List<Attendance>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            records.forEach { record ->
                val docRef = firestore.collection("attendance").document()
                batch.set(docRef, record.copy(attendanceId = docRef.id))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttendanceHistory(classId: String): List<Attendance> {
        return try {
            firestore.collection("attendance")
                .whereEqualTo("classId", classId)
                .get()
                .await()
                .toObjects(Attendance::class.java)
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
