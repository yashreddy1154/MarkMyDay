package com.project.markmyday.data.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val results: List<Article>
)

data class Article(
    val title: String?,
    val description: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("pubDate")
    val pubDate: String?,
    val link: String?
)
