package com.example.stockdemo.feature.auth.domain.repository

import com.example.stockdemo.feature.auth.domain.model.LoginRequest
import com.example.stockdemo.feature.auth.domain.model.User
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun login(loginRequest: LoginRequest): Flow<Resource<User>>
}


