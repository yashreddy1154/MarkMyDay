package com.project.markmyday.data.api

import com.project.markmyday.data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("news")
    suspend fun getNews(
        @Query("apikey") apiKey: String = "pub_0c15e90006244d749b4ae4ddf391f7f1",
        @Query("country") country: String = "in"
    ): Response<NewsResponse>
}
