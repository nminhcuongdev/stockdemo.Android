package com.example.satostockmanagement.viewmodel.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.satostockmanagement.repository.UserRepository
import com.example.satostockmanagement.models.users.Login
import com.example.satostockmanagement.screens.LoginUiState
import com.example.satostockmanagement.session.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel (private val repository: UserRepository, private val userSession: UserSession): ViewModel() {

    val userName: Flow<String?> = userSession.userName
    val userId: Flow<Int?> = userSession.userId
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun login(username: String, pass: String) {

        // Bắt đầu bấm nút -> Chuyển sang Loading
        uiState = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.login(Login(username, pass))

            // Dựa vào kết quả từ Repository để cập nhật UiState
            uiState = result.fold(
                onSuccess = {
                    userSession.saveSession(it.fullName, it.userId)
                    LoginUiState.Success(it)
                },
                onFailure = { LoginUiState.Error(it.message ?: "Lỗi kết nối") }
            )
        }
    }

    // Thêm logic logout nếu cần
    fun logout() {
        viewModelScope.launch {
            userSession.clearSession()
            uiState = LoginUiState.Idle
        }
    }

    fun resetState() {
        uiState = LoginUiState.Idle
    }

}