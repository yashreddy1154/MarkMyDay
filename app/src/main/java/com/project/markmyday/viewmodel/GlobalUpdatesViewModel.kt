package com.project.markmyday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.api.RetrofitInstance
import com.project.markmyday.data.model.GNewsArticle
import com.project.markmyday.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<GNewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}

class GlobalUpdatesViewModel : ViewModel() {
    private val repository = NewsRepository(RetrofitInstance.api)

    private val _newsState = MutableStateFlow<NewsState>(NewsState.Loading)
    val newsState: StateFlow<NewsState> = _newsState.asStateFlow()

    init {
        fetchNews()
    }

    fun fetchNews() {
        viewModelScope.launch {
            _newsState.value = NewsState.Loading
            try {
                val response = repository.getNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    _newsState.value = NewsState.Success(articles)
                } else {
                    val errorMsg = "Error: ${response.code()} ${response.message()}"
                    Log.e("GlobalUpdatesVM", errorMsg)
                    _newsState.value = NewsState.Error(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesVM", "Exception: ${e.message}", e)
                _newsState.value = NewsState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }
}
