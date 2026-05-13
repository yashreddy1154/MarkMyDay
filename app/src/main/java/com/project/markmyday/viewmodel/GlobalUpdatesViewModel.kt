package com.project.markmyday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.markmyday.data.api.RetrofitInstance
import com.project.markmyday.data.model.GNewsArticle
import com.project.markmyday.data.model.WeatherData
import com.project.markmyday.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<GNewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}

class GlobalUpdatesViewModel : ViewModel() {
    private val repository = NewsRepository(RetrofitInstance.api)

    private val _hotNews = MutableStateFlow<NewsState>(NewsState.Loading)
    val hotNews: StateFlow<NewsState> = _hotNews.asStateFlow()

    private val _nationalNews = MutableStateFlow<NewsState>(NewsState.Loading)
    val nationalNews: StateFlow<NewsState> = _nationalNews.asStateFlow()

    private val _internationalNews = MutableStateFlow<NewsState>(NewsState.Loading)
    val internationalNews: StateFlow<NewsState> = _internationalNews.asStateFlow()

    private val _gkNews = MutableStateFlow<NewsState>(NewsState.Loading)
    val gkNews: StateFlow<NewsState> = _gkNews.asStateFlow()

    private val _schoolNews = MutableStateFlow<NewsState>(NewsState.Loading)
    val schoolNews: StateFlow<NewsState> = _schoolNews.asStateFlow()

    private val _sscNews = MutableStateFlow<NewsState>(NewsState.Loading)
    val sscNews: StateFlow<NewsState> = _sscNews.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherData?>(null)
    val weatherState: StateFlow<WeatherData?> = _weatherState.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        updateDateTime()
        fetchWeather()
        fetchAllNews()
    }

    private fun updateDateTime() {
        val today = LocalDate.now()
        _currentDate.value = today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }

    private fun fetchWeather() {
        // Mock weather for now - in a real app, use FusedLocation and a Weather API
        _weatherState.value = WeatherData(
            temperature = "28°C",
            condition = "Partly Cloudy",
            city = "Hyderabad",
        )
    }

    fun fetchAllNews() {
        fetchHotNews()
        fetchNationalNews()
        fetchInternationalNews()
        fetchGKNews()
        fetchSchoolNews()
        fetchSSCNews()
    }

    private fun fetchHotNews() {
        viewModelScope.launch {
            _hotNews.value = NewsState.Loading
            try {
                val response = repository.getHotNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("GlobalUpdatesViewModel", "Hot News Success: ${articles.size} articles")
                    _hotNews.value = NewsState.Success(articles)
                } else {
                    Log.e("GlobalUpdatesViewModel", "Hot News Error: ${response.code()} ${response.message()}")
                    _hotNews.value = NewsState.Error("Failed to fetch hot news")
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesViewModel", "Hot News Exception", e)
                _hotNews.value = NewsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchNationalNews() {
        viewModelScope.launch {
            _nationalNews.value = NewsState.Loading
            try {
                val response = repository.getNationalNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("GlobalUpdatesViewModel", "National News Success: ${articles.size} articles")
                    _nationalNews.value = NewsState.Success(articles)
                } else {
                    Log.e("GlobalUpdatesViewModel", "National News Error: ${response.code()}")
                    _nationalNews.value = NewsState.Error("Failed to fetch national news")
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesViewModel", "National News Exception", e)
                _nationalNews.value = NewsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchInternationalNews() {
        viewModelScope.launch {
            _internationalNews.value = NewsState.Loading
            try {
                val response = repository.getInternationalNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("GlobalUpdatesViewModel", "International News Success: ${articles.size} articles")
                    _internationalNews.value = NewsState.Success(articles)
                } else {
                    Log.e("GlobalUpdatesViewModel", "International News Error: ${response.code()}")
                    _internationalNews.value = NewsState.Error("Failed to fetch international news")
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesViewModel", "International News Exception", e)
                _internationalNews.value = NewsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchGKNews() {
        viewModelScope.launch {
            _gkNews.value = NewsState.Loading
            try {
                val response = repository.getGKNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("GlobalUpdatesViewModel", "GK News Success: ${articles.size} articles")
                    _gkNews.value = NewsState.Success(articles)
                } else {
                    Log.e("GlobalUpdatesViewModel", "GK News Error: ${response.code()}")
                    _gkNews.value = NewsState.Error("Failed to fetch GK")
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesViewModel", "GK News Exception", e)
                _gkNews.value = NewsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchSchoolNews() {
        viewModelScope.launch {
            _schoolNews.value = NewsState.Loading
            try {
                val response = repository.getSchoolNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("GlobalUpdatesViewModel", "School News Success: ${articles.size} articles")
                    _schoolNews.value = NewsState.Success(articles)
                } else {
                    Log.e("GlobalUpdatesViewModel", "School News Error: ${response.code()}")
                    _schoolNews.value = NewsState.Error("Failed to fetch school news")
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesViewModel", "School News Exception", e)
                _schoolNews.value = NewsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchSSCNews() {
        viewModelScope.launch {
            _sscNews.value = NewsState.Loading
            try {
                val response = repository.getSSCNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    Log.d("GlobalUpdatesViewModel", "SSC News Success: ${articles.size} articles")
                    _sscNews.value = NewsState.Success(articles)
                } else {
                    Log.e("GlobalUpdatesViewModel", "SSC News Error: ${response.code()}")
                    _sscNews.value = NewsState.Error("Failed to fetch SSC news")
                }
            } catch (e: Exception) {
                Log.e("GlobalUpdatesViewModel", "SSC News Exception", e)
                _sscNews.value = NewsState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // This property is kept for backward compatibility if needed, but we prefer specific ones now
    private val _newsState = MutableStateFlow<NewsState>(NewsState.Loading)
    val newsState: StateFlow<NewsState> = _newsState.asStateFlow()
    
    fun fetchNews() = fetchAllNews()
}
