package com.example.stockdemo.feature.chat.data.repository

import com.example.stockdemo.feature.chat.data.mapper.toDomain
import com.example.stockdemo.feature.chat.data.mapper.toDto
import com.example.stockdemo.feature.chat.data.remote.PythonApiService
import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import com.example.stockdemo.feature.chat.domain.model.ChatResponse
import com.example.stockdemo.feature.chat.domain.model.HealthResponse
import com.example.stockdemo.feature.chat.domain.repository.ChatRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class ChatRepositoryImpl(
    private val api: PythonApiService
) : ChatRepository {

    override fun checkHealth(): Flow<Resource<HealthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val responseDto = api.checkHealth()
            val response = HealthResponse(
                status = responseDto.status,
                version = responseDto.version
            )
            emit(Resource.Success(response))
        } catch (e: Exception) {
            when (e) {
                is HttpException -> emit(Resource.Error("Lỗi kết nối server Python"))
                is IOException -> emit(Resource.Error("Lỗi mạng"))
                else -> emit(Resource.Error(e.message ?: "Lỗi không xác định"))
            }
        }
    }

    override fun chat(request: ChatRequest): Flow<Resource<ChatResponse>> = flow {
        emit(Resource.Loading())
        try {
            val responseDto = api.chat(request.toDto())
            emit(Resource.Success(responseDto.toDomain()))
        } catch (e: Exception) {
            when (e) {
                is HttpException -> emit(Resource.Error("Lỗi kết nối server Python"))
                is IOException -> emit(Resource.Error("Lỗi mạng"))
                else -> emit(Resource.Error(e.message ?: "Lỗi không xác định"))
            }
        }
    }
}



