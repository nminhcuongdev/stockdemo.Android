package com.example.satostockmanagement.repository

import com.example.satostockmanagement.retrofit.RetrofitInstance
import com.example.satostockmanagement.models.users.Login
import com.example.satostockmanagement.models.users.User

class UserRepository {
    private val apiService = RetrofitInstance.api

    suspend fun login(login: Login): Result<User> {
        return try {
            val response = apiService.login(login)

            if (response.success && response.data != null) {
                // Đăng nhập thành công theo logic của Server
                Result.success(response.data)
            } else {
                // Server trả về success: false hoặc data null
                val errorMsg = response.message ?: "Đăng nhập thất bại"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Lỗi kết nối hoặc lỗi parsing JSON
            Result.failure(e)
        }
    }

}