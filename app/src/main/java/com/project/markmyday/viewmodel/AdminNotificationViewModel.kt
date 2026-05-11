package com.project.markmyday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.project.markmyday.data.model.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class NotificationUiState {
    object Idle : NotificationUiState()
    object Loading : NotificationUiState()
    object Success : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

class AdminNotificationViewModel : ViewModel() {
    // Reference to Realtime Database
    private val database = FirebaseDatabase.getInstance().getReference("notifications")

    private val _heading = MutableStateFlow("")
    val heading = _heading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _author = MutableStateFlow("")
    val author = _author.asStateFlow()

    private val _targetAudience = MutableStateFlow("all")
    val targetAudience = _targetAudience.asStateFlow()

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        fetchHistory()
    }

    private fun fetchHistory() {
        database.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<NotificationData>()
                for (doc in snapshot.children) {
                    try {
                        // CRITICAL FIX: Check if data is an object before parsing
                        if (doc.value is String) {
                            Log.e("AdminNotificationVM", "Invalid data type (String) at key: ${doc.key}. Skipping.")
                            continue
                        }
                        
                        doc.getValue(NotificationData::class.java)?.let { list.add(it) }
                    } catch (e: Exception) {
                        Log.e("AdminNotificationVM", "Error parsing history item: ${doc.key}", e)
                    }
                }
                _notifications.value = list.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AdminNotificationVM", "History fetch cancelled", error.toException())
            }
        })
    }

    fun onHeadingChange(newValue: String) {
        _heading.value = newValue
    }

    fun onMessageChange(newValue: String) {
        _message.value = newValue
    }

    fun onAuthorChange(newValue: String) {
        _author.value = newValue
    }

    fun onTargetAudienceChange(newTarget: String) {
        _targetAudience.value = newTarget
    }

    fun sendNotification() {
        val currentHeading = _heading.value
        val currentMessage = _message.value
        val currentAuthor = _author.value
        val currentAudience = _targetAudience.value

        if (currentHeading.isBlank() || currentMessage.isBlank() || currentAuthor.isBlank()) {
            _uiState.value = NotificationUiState.Error("All fields are required")
            return
        }

        // 5. Debug Log: Start
        Log.d("AdminNotificationVM", "Attempting to send notification: $currentHeading")
        
        _uiState.value = NotificationUiState.Loading

        // 3. Generate ID using push()
        val notificationId = database.push().key
        if (notificationId == null) {
            Log.e("AdminNotificationVM", "Failed to generate Firebase push key")
            _uiState.value = NotificationUiState.Error("Database error: Could not generate ID")
            return
        }

        val notification = NotificationData(
            id = notificationId,
            heading = currentHeading,
            message = currentMessage,
            author = currentAuthor,
            audience = currentAudience,
            timestamp = System.currentTimeMillis()
        )

        // 1. Use Listeners (NOT .await())
        database.child(notificationId).setValue(notification)
            .addOnSuccessListener {
                // 5. Debug Log: Success
                Log.d("AdminNotificationVM", "Notification successfully saved! ID: $notificationId")
                
                // Reset input fields after success
                _heading.value = ""
                _message.value = ""
                _author.value = ""
                _uiState.value = NotificationUiState.Success
            }
            .addOnFailureListener { e ->
                // 5. Debug Log: Error
                Log.e("AdminNotificationVM", "Failed to save notification", e)
                _uiState.value = NotificationUiState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
    }

    fun deleteNotification(id: String) {
        database.child(id).removeValue()
            .addOnSuccessListener {
                Log.d("AdminNotificationVM", "Notification deleted: $id")
            }
            .addOnFailureListener { e ->
                Log.e("AdminNotificationVM", "Delete failed", e)
            }
    }

    fun resetUiState() {
        _uiState.value = NotificationUiState.Idle
    }
}
