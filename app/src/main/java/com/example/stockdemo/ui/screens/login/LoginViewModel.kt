package com.example.stockdemo.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.domain.model.auth.LoginRequest
import com.example.stockdemo.domain.usecase.auth.LoginUseCase
import com.example.stockdemo.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        val loginRequest = LoginRequest(username, password)
        loginUseCase(loginRequest).onEach { result ->
            _uiState.value = when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        LoginUiState.Success(result.data)
                    } else {
                        LoginUiState.Error("User data is null")
                    }
                }
                is Resource.Error -> {
                    LoginUiState.Error(result.message ?: "Login failed")
                }
                is Resource.Loading -> {
                    if (result.isLoading) LoginUiState.Loading else _uiState.value
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}