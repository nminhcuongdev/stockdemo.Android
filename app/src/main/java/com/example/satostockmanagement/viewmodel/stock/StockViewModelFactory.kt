package com.example.satostockmanagement.viewmodel.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.satostockmanagement.repository.StockRepository
import com.example.satostockmanagement.repository.UserRepository
import com.example.satostockmanagement.session.UserSession
import com.example.satostockmanagement.viewmodel.user.UserViewModel

class StockViewModelFactory(
    private val repository: StockRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}