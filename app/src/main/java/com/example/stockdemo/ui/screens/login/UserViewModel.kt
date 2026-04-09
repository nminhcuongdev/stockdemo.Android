package com.example.stockdemo.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val userName: Flow<String?> = userPreferences.userName
    val userId: Flow<Int?> = userPreferences.userId

    fun saveUser(name: String, id: Int) {
        viewModelScope.launch {
            userPreferences.saveUser(name, id)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clear()
        }
    }
}