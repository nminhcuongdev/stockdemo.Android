package com.example.stockdemo.domain.use_case.chat

import com.example.stockdemo.domain.model.chat.HealthResponse
import com.example.stockdemo.domain.repository.ChatRepository
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckChatHealthUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<Resource<HealthResponse>> {
        return repository.checkHealth()
    }
}
