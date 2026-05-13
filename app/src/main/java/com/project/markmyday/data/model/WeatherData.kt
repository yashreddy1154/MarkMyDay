package com.project.markmyday.data.model

data class WeatherData(
    val temperature: String,
    val condition: String,
    val city: String,
    val icon: String? = null
)
