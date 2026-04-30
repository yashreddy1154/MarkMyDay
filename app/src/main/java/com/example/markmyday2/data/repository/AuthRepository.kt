package com.example.markmyday2.data.repository

import com.example.markmyday2.data.model.User
import com.example.markmyday2.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser get() = auth.currentUser

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val userId = authResult.user?.uid ?: throw Exception("Login failed")
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDetails(userId: String): User? {
        return try {
            firestore.collection("users").document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }
}
