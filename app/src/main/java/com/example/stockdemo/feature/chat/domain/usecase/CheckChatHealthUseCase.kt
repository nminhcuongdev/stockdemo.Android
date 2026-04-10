package com.example.stockdemo.feature.chat.domain.usecase

import com.example.stockdemo.feature.chat.domain.model.HealthResponse
import com.example.stockdemo.feature.chat.domain.repository.ChatRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckChatHealthUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<Resource<HealthResponse>> {
        return repository.checkHealth()
    }
}



