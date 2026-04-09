package com.example.stockdemo.data.repository

import com.example.stockdemo.data.mapper.toDomain
import com.example.stockdemo.data.remote.dto.ApiService
import com.example.stockdemo.data.remote.dto.BaseResponse
import com.example.stockdemo.data.remote.dto.ValidationErrorResponse
import com.example.stockdemo.domain.model.auth.LoginRequest
import com.example.stockdemo.domain.model.user.User
import com.example.stockdemo.domain.repository.UserRepository
import com.example.stockdemo.domain.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class UserRepositoryImpl(
    private val api: ApiService
) : UserRepository {

    override fun login(loginRequest: LoginRequest): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(loginRequest)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toDomain()))
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
