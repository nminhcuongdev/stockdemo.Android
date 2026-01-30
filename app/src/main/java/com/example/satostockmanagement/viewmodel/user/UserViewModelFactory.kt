package com.example.satostockmanagement.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.satostockmanagement.repository.UserRepository
import com.example.satostockmanagement.session.UserSession

class UserViewModelFactory(
    private val repository: UserRepository,
    private val userSession: UserSession // Thêm tham số này
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Truyền cả 2 tham số vào constructor của UserViewModel
            return UserViewModel(repository, userSession) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}