package com.project.markmyday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.markmyday.data.model.Admission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdmissionsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val admissionsCollection = firestore.collection("admissions")

    private val _admissions = MutableStateFlow<List<Admission>>(emptyList())
    val admissions: StateFlow<List<Admission>> = _admissions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAdmissions()
    }

    fun fetchAdmissions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = admissionsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val admissionList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Admission::class.java)?.copy(id = doc.id)
                }
                _admissions.value = admissionList
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAdmission(admission: Admission) {
        viewModelScope.launch {
            try {
                admissionsCollection.add(admission).await()
                fetchAdmissions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateAdmission(admission: Admission) {
        viewModelScope.launch {
            try {
                admissionsCollection.document(admission.id).set(admission).await()
                fetchAdmissions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAdmission(id: String) {
        viewModelScope.launch {
            try {
                admissionsCollection.document(id).delete().await()
                fetchAdmissions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
