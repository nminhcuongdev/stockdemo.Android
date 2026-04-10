package com.example.stockdemo.feature.chat.domain.repository

import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import com.example.stockdemo.feature.chat.domain.model.ChatResponse
import com.example.stockdemo.feature.chat.domain.model.HealthResponse
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun checkHealth(): Flow<Resource<HealthResponse>>
    fun chat(request: ChatRequest): Flow<Resource<ChatResponse>>
}



