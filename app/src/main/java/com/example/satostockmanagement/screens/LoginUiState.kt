package com.example.satostockmanagement.screens

import com.example.satostockmanagement.models.users.User

sealed interface LoginUiState {
    object Idle : LoginUiState

    // 2. Trạng thái đang gửi dữ liệu lên server
    object Loading : LoginUiState

    // 3. Trạng thái thành công, mang theo dữ liệu User trả về
    data class Success(val user: User) : LoginUiState

    // 4. Trạng thái lỗi, mang theo thông báo lỗi để hiển thị
    data class Error(val message: String) : LoginUiState
}