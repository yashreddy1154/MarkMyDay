package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.subscribeUserToTopics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(
        val name: String,
        val role: String,
        val homeSection: String? = null,
        val subject: String? = null
    ) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authState: StateFlow<AuthResult> = _authState.asStateFlow()

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            try {
                // 1. Sign in with email and password
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid

                if (uid != null) {
                    fetchUserAndSetState(uid)
                } else {
                    _authState.value = AuthResult.Error("Failed to get User ID after sign-in.")
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error(e.localizedMessage ?: "An unknown error occurred")
            }
        }
    }

    fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                _authState.value = AuthResult.Loading
                fetchUserAndSetState(currentUser.uid)
            }
        } else {
            _authState.value = AuthResult.Idle
        }
    }

    private suspend fun fetchUserAndSetState(uid: String) {
        try {
            // 2. Fetch user document from Firestore
            val document = firestore.collection("users").document(uid).get().await()

            if (document.exists()) {
                // 3. Extract name, role, and teacher specific fields
                val name = document.getString("name") ?: "User"
                val role = document.getString("role") ?: "unknown"
                val homeSection = document.getString("homeSection")
                val subject = document.getString("subject")

                subscribeUserToTopics(role)

                _authState.value = AuthResult.Success(name, role, homeSection, subject)
            } else {
                _authState.value = AuthResult.Error("User document not found in Firestore.")
            }
        } catch (e: Exception) {
            _authState.value = AuthResult.Error(e.localizedMessage ?: "An unknown error occurred")
        }
    }
}
