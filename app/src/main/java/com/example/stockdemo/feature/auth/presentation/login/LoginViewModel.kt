package com.example.stockdemo.feature.auth.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.auth.domain.model.LoginRequest
import com.example.stockdemo.feature.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    @ApplicationContext private val context: Context
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
                        LoginUiState.Error(context.getString(R.string.login_user_data_null))
                    }
                }
                is Resource.Error -> {
                    LoginUiState.Error(result.message ?: context.getString(R.string.login_failed))
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
