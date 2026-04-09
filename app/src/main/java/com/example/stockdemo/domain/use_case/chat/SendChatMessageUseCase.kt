package com.example.stockdemo.domain.use_case.chat

import com.example.stockdemo.domain.model.chat.ChatRequest
import com.example.stockdemo.domain.model.chat.ChatResponse
import com.example.stockdemo.domain.repository.ChatRepository
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(request: ChatRequest): Flow<Resource<ChatResponse>> {
        return repository.chat(request)
    }
}
