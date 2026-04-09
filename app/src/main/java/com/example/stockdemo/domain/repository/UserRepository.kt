package com.example.stockdemo.domain.repository

import com.example.stockdemo.domain.model.auth.LoginRequest
import com.example.stockdemo.domain.model.user.User
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun login(loginRequest: LoginRequest): Flow<Resource<User>>
}