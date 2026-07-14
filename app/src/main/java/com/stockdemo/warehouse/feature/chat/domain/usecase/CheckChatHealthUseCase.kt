package com.stockdemo.warehouse.feature.chat.domain.usecase

import com.stockdemo.warehouse.feature.chat.domain.model.HealthResponse
import com.stockdemo.warehouse.feature.chat.domain.repository.ChatRepository
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckChatHealthUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<Resource<HealthResponse>> {
        return repository.checkHealth()
    }
}



