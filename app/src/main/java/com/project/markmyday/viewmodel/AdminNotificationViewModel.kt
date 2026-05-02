package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class NotificationUiState {
    object Idle : NotificationUiState()
    object Loading : NotificationUiState()
    object Success : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

class AdminNotificationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _targetAudience = MutableStateFlow("all")
    val targetAudience = _targetAudience.asStateFlow()

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onMessageChange(newMessage: String) {
        _message.value = newMessage
    }

    fun onTargetAudienceChange(newTarget: String) {
        _targetAudience.value = newTarget
    }

    fun sendNotification() {
        val currentMessage = _message.value
        val currentAudience = _targetAudience.value

        if (currentMessage.isBlank()) {
            _uiState.value = NotificationUiState.Error("Message cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            try {
                val notificationData = hashMapOf(
                    "message" to currentMessage,
                    "target_audience" to currentAudience,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("notifications").add(notificationData).await()
                _message.value = ""
                _uiState.value = NotificationUiState.Success
            } catch (e: Exception) {
                _uiState.value = NotificationUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = NotificationUiState.Idle
    }
}
