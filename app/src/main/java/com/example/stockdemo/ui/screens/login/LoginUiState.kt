package com.example.stockdemo.ui.screens.login

import com.example.stockdemo.domain.model.user.User

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}