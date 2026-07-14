package com.stockdemo.warehouse.feature.auth.domain.repository

import com.stockdemo.warehouse.feature.auth.domain.model.LoginRequest
import com.stockdemo.warehouse.feature.auth.domain.model.User
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun login(loginRequest: LoginRequest): Flow<Resource<User>>
}


