package com.project.markmyday.data.api

import com.project.markmyday.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("news")
    suspend fun getNews(
        @Query("apikey") apiKey: String = "pub_8bbc11822f364b738dbc6cf92288559f",
        @Query("country") country: String = "in"
    ): Response<NewsResponse>
}
