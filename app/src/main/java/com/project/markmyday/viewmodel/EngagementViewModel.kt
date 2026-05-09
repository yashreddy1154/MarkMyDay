package com.project.markmyday.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.model.StudentEngagementSummary
import com.project.markmyday.data.repository.EngagementRepository
import com.project.markmyday.utils.CsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EngagementViewModel : ViewModel() {
    private val repository = EngagementRepository()

    private val _engagementSummaries = MutableStateFlow<List<StudentEngagementSummary>>(emptyList())
    val engagementSummaries: StateFlow<List<StudentEngagementSummary>> = _engagementSummaries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchEngagement(className: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getEngagementForClass(className)
            _engagementSummaries.value = data
            _isLoading.value = false
        }
    }

    fun exportReport(context: Context) {
        val summaries = _engagementSummaries.value
        if (summaries.isEmpty()) return

        val uri = CsvExporter.generateEngagementCsv(context, summaries)
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Engagement Report"))
        }
    }
}
