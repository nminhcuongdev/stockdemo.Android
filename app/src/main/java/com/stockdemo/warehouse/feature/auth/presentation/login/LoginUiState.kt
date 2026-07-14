package com.stockdemo.warehouse.feature.auth.presentation.login

import com.stockdemo.warehouse.core.ui.UiText
import com.stockdemo.warehouse.feature.auth.domain.model.User

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: UiText) : LoginUiState()
}


