package com.project.markmyday.data.repository

import com.project.markmyday.data.api.NewsApiService
import com.project.markmyday.data.model.NewsResponse
import retrofit2.Response

class NewsRepository(private val apiService: NewsApiService) {
    
    suspend fun getHotNews(): Response<NewsResponse> {
        return apiService.getTopHeadlines(category = "general", country = "in")
    }

    suspend fun getSchoolNews(): Response<NewsResponse> {
        return apiService.searchNews(query = "CBSE OR \"exam results\" OR \"board results\" OR \"academic updates\"", country = "in")
    }

    suspend fun getSSCNews(): Response<NewsResponse> {
        return apiService.searchNews(query = "SSC OR \"Staff Selection Commission\" OR \"SSC CGL\" OR \"SSC CHSL\" OR \"SSC GD\"", country = "in")
    }

    suspend fun getGKNews(): Response<NewsResponse> {
        return apiService.searchNews(query = "\"general knowledge\" OR \"current affairs\" OR \"facts for students\"")
    }

    suspend fun getNationalNews(): Response<NewsResponse> {
        return apiService.getTopHeadlines(category = "nation", country = "in")
    }

    suspend fun getInternationalNews(): Response<NewsResponse> {
        return apiService.getTopHeadlines(category = "world")
    }
}
