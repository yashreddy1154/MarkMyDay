package com.project.markmyday.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.markmyday.data.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val profile: UserProfile) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

class SettingsViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _currentLanguage = MutableStateFlow(getCurrentLocale())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    val document = firestore.collection("users").document(uid).get().await()
                    if (document.exists()) {
                        val name = document.getString("name") ?: "Unknown"
                        val role = document.getString("role") ?: "User"
                        val profile = UserProfile(name, role)
                        _uiState.value = SettingsUiState.Success(profile)
                    } else {
                        _uiState.value = SettingsUiState.Error("User profile not found.")
                    }
                } else {
                    _uiState.value = SettingsUiState.Error("User not authenticated.")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.localizedMessage ?: "An error occurred.")
            }
        }
    }

    fun setLanguage(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _currentLanguage.value = languageCode
    }

    fun logout() {
        auth.signOut()
        viewModelScope.launch {
            _logoutEvent.emit(Unit)
        }
    }

    private fun getCurrentLocale(): String {
        return AppCompatDelegate.getApplicationLocales().toLanguageTags() ?: "en"
    }
}
