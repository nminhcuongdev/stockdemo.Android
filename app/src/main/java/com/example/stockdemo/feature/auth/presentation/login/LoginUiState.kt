package com.example.stockdemo.feature.auth.presentation.login

import com.example.stockdemo.core.ui.UiText
import com.example.stockdemo.feature.auth.domain.model.User

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: UiText) : LoginUiState()
}


