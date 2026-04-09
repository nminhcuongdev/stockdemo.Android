package com.example.stockdemo.domain.usecase.auth

import com.example.stockdemo.domain.model.auth.LoginRequest
import com.example.stockdemo.domain.model.user.User
import com.example.stockdemo.domain.repository.UserRepository
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(loginRequest: LoginRequest): Flow<Resource<User>> {
        return repository.login(loginRequest)
    }
}