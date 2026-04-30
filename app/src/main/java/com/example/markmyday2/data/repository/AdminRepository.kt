package com.example.markmyday2.data.repository

import com.example.markmyday2.data.model.ClassInfo
import com.example.markmyday2.data.model.TimetableEntry
import com.example.markmyday2.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun createTeacher(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createStudent(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.userId).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return firestore.collection("users").get().await().toObjects(User::class.java)
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createClass(classInfo: ClassInfo): Result<Unit> {
        return try {
            val docRef = firestore.collection("classes").document()
            val newClass = classInfo.copy(classId = docRef.id)
            docRef.set(newClass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllClasses(): List<ClassInfo> {
        return firestore.collection("classes").get().await().toObjects(ClassInfo::class.java)
    }

    suspend fun deleteClass(classId: String): Result<Unit> {
        return try {
            firestore.collection("classes").document(classId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTimetableEntry(entry: TimetableEntry): Result<Unit> {
        return try {
            firestore.collection("timetable").add(entry).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
