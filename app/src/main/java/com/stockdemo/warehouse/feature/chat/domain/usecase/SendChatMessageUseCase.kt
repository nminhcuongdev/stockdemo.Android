package com.stockdemo.warehouse.feature.chat.domain.usecase

import com.stockdemo.warehouse.feature.chat.domain.model.ChatRequest
import com.stockdemo.warehouse.feature.chat.domain.model.ChatResponse
import com.stockdemo.warehouse.feature.chat.domain.repository.ChatRepository
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(request: ChatRequest): Flow<Resource<ChatResponse>> {
        return repository.chat(request)
    }
}



