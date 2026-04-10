package com.example.stockdemo.feature.chat.domain.usecase

import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import com.example.stockdemo.feature.chat.domain.model.ChatResponse
import com.example.stockdemo.feature.chat.domain.repository.ChatRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(request: ChatRequest): Flow<Resource<ChatResponse>> {
        return repository.chat(request)
    }
}



