package com.example.stockdemo.feature.chat.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PythonApiService {
    @GET("health")
    suspend fun checkHealth(): HealthResponseDto

    @POST("chat")
    suspend fun chat(@Body request: ChatRequestDto): ChatResponseDto
}



