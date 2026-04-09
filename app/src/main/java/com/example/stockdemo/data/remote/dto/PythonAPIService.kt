package com.example.stockdemo.data.remote.dto

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PythonAPIService {
    @GET("health")
    suspend fun checkHealth(): HealthResponseDto

    @POST("chat")
    suspend fun chat(@Body request: ChatRequestDto): ChatResponseDto
}
