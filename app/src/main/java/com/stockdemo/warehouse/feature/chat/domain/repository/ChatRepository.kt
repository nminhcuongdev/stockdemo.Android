package com.stockdemo.warehouse.feature.chat.domain.repository

import com.stockdemo.warehouse.feature.chat.domain.model.ChatRequest
import com.stockdemo.warehouse.feature.chat.domain.model.ChatResponse
import com.stockdemo.warehouse.feature.chat.domain.model.HealthResponse
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun checkHealth(): Flow<Resource<HealthResponse>>
    fun chat(request: ChatRequest): Flow<Resource<ChatResponse>>
}



