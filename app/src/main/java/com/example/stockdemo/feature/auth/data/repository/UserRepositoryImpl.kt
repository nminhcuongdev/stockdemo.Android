package com.example.stockdemo.feature.auth.data.repository

import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.data.mapper.toDomain
import com.example.stockdemo.feature.stock.data.remote.ApiService
import com.example.stockdemo.core.network.model.BaseResponse
import com.example.stockdemo.core.network.model.ValidationErrorResponse
import com.example.stockdemo.feature.auth.domain.model.LoginRequest
import com.example.stockdemo.feature.auth.domain.model.User
import com.example.stockdemo.feature.auth.domain.repository.UserRepository
import com.example.stockdemo.core.common.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val api: ApiService,
    private val userPreferences: UserPreferences
) : UserRepository {

    override fun login(loginRequest: LoginRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(loginRequest)
            if (response.success && response.data != null) {
                val token = response.data.accessToken
                val userDto = response.data.resolveUser()

                if (token.isNullOrBlank()) {
                    emit(Resource.Error("Phản hồi đăng nhập không có access token"))
                } else if (userDto == null) {
                    emit(Resource.Error("Phản hồi đăng nhập không có thông tin người dùng"))
                } else {
                    userPreferences.saveAccessToken(token)
                    userPreferences.saveUser(userDto.fullName, userDto.userId)
                    emit(Resource.Success(userDto.toDomain()))
                }
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = when (e.code()) {
                400 -> parseValidationError(errorBody)
                401 -> parseBaseError(errorBody) ?: "Tên đăng nhập hoặc mật khẩu không đúng"
                500 -> "Lỗi hệ thống server (500)"
                else -> "Lỗi kết nối: ${e.code()}"
            }
            emit(Resource.Error(errorMessage))
        } catch (e: IOException) {
            emit(Resource.Error("Không thể kết nối server. Vui lòng kiểm tra internet hoặc IP server."))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi không xác định"))
        }
    }

    private fun parseValidationError(errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, ValidationErrorResponse::class.java)
            errorResponse.errors?.values?.flatten()?.joinToString("\n") 
                ?: "Dữ liệu nhập vào không hợp lệ"
        } catch (e: Exception) {
            "Lỗi định dạng dữ liệu (400)"
        }
    }

    private fun parseBaseError(errorBody: String?): String? {
        return try {
            val response = Gson().fromJson(errorBody, BaseResponse::class.java)
            response.message
        } catch (e: Exception) {
            null
        }
    }
}



