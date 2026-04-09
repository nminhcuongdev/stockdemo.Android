package com.example.stockdemo.domain.repository

import com.example.stockdemo.domain.model.chat.ChatRequest
import com.example.stockdemo.domain.model.chat.ChatResponse
import com.example.stockdemo.domain.model.chat.HealthResponse
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun checkHealth(): Flow<Resource<HealthResponse>>
    fun chat(request: ChatRequest): Flow<Resource<ChatResponse>>
}
