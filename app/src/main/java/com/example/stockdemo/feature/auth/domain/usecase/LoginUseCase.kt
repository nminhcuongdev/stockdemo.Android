package com.example.stockdemo.feature.auth.domain.usecase

import com.example.stockdemo.feature.auth.domain.model.LoginRequest
import com.example.stockdemo.feature.auth.domain.model.User
import com.example.stockdemo.feature.auth.domain.repository.UserRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(loginRequest: LoginRequest): Flow<Resource<User>> {
        return repository.login(loginRequest)
    }
}


