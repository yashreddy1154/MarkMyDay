package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    data class Success(val name: String, val role: String) : AuthResult()
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
                    // 2. Fetch user document from Firestore
                    val document = firestore.collection("users").document(uid).get().await()
                    
                    if (document.exists()) {
                        // 3. Extract name and role strings
                        val name = document.getString("name") ?: "User"
                        val role = document.getString("role") ?: "unknown"
                        _authState.value = AuthResult.Success(name, role)
                    } else {
                        _authState.value = AuthResult.Error("User document not found in Firestore.")
                    }
                } else {
                    _authState.value = AuthResult.Error("Failed to get User ID after sign-in.")
                }
            } catch (e: Exception) {
                _authState.value = AuthResult.Error(e.localizedMessage ?: "An unknown error occurred")
            }
        }
    }
}
