package com.project.markmyday.data.repository

import com.project.markmyday.data.api.NewsApiService
import com.project.markmyday.data.model.NewsResponse
import retrofit2.Response

class NewsRepository(private val apiService: NewsApiService) {
    suspend fun getNews(): Response<NewsResponse> {
        return apiService.getNews()
    }
}
