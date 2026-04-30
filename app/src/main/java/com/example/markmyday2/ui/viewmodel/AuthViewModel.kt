package com.example.markmyday2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.markmyday2.data.model.User
import com.example.markmyday2.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    val userState: StateFlow<UserState> = _userState

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val firebaseUser = repository.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                val user = repository.getUserDetails(firebaseUser.uid)
                if (user != null) {
                    _userState.value = UserState.Authenticated(user)
                } else {
                    _userState.value = UserState.Idle
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.login(email, pass)
            result.onSuccess { user ->
                _userState.value = UserState.Authenticated(user)
            }.onFailure { error ->
                _userState.value = UserState.Error(error.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        _userState.value = UserState.Idle
    }

    fun loginAsDemo(role: com.example.markmyday2.data.model.UserRole, demoId: String = "demo_id", name: String? = null) {
        val dummyUser = User(
            userId = demoId,
            name = name ?: "Demo ${role.name}",
            email = "demo_${demoId}@example.com",
            role = role
        )
        _userState.value = UserState.Authenticated(dummyUser)
    }
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Authenticated(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}
