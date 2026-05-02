package com.project.markmyday.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Notification(
    val id: String = "",
    val message: String = "",
    val targetAudience: String = "",
    val timestamp: Timestamp? = null
)

class NotificationViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val prefs = application.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _hasUnreadNotices = MutableStateFlow(false)
    val hasUnreadNotices = _hasUnreadNotices.asStateFlow()

    private var lastReadTimestamp: Long
        get() = prefs.getLong("last_read_timestamp", 0L)
        set(value) = prefs.edit().putLong("last_read_timestamp", value).apply()

    fun fetchNotifications(role: String) {
        viewModelScope.launch {
            try {
                val query = firestore.collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)

                val filteredQuery = when (role.lowercase()) {
                    "teacher" -> query.whereIn("target_audience", listOf("all", "teachers"))
                    "principal", "headmaster", "admin" -> query // Admins see everything
                    else -> query.whereEqualTo("target_audience", "all") // Students/others
                }

                val snapshot = filteredQuery.get().await()
                val fetchedNotifications = snapshot.documents.map { doc ->
                    Notification(
                        id = doc.id,
                        message = doc.getString("message") ?: "",
                        targetAudience = doc.getString("target_audience") ?: "",
                        timestamp = doc.getTimestamp("timestamp")
                    )
                }

                _notifications.value = fetchedNotifications
                checkUnreadStatus(fetchedNotifications)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun checkUnreadStatus(list: List<Notification>) {
        if (list.isEmpty()) {
            _hasUnreadNotices.value = false
            return
        }
        val newestTimestamp = list.first().timestamp?.seconds ?: 0L
        _hasUnreadNotices.value = newestTimestamp > lastReadTimestamp
    }

    fun markAsRead() {
        val newestTimestamp = _notifications.value.firstOrNull()?.timestamp?.seconds ?: (System.currentTimeMillis() / 1000)
        lastReadTimestamp = newestTimestamp
        _hasUnreadNotices.value = false
    }
}
