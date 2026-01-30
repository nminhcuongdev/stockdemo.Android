package com.example.satostockmanagement.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.satostockmanagement.models.UserSettings
class WarehouseViewModel : ViewModel() {
    var username = mutableStateOf("")
    var isLoggedIn = mutableStateOf(false)
    var settings = mutableStateOf(UserSettings())

    fun updateSettings(newSettings: UserSettings) {
        settings.value = newSettings
    }
}