package com.example.stockdemo.feature.home.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.domain.model.DashboardStats
import com.example.stockdemo.feature.stock.domain.model.LowStockItem
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.feature.stock.domain.usecase.GetLowStockAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    stockRepository: StockRepository,
    userPreferences: UserPreferences,
    getLowStockAlertsUseCase: GetLowStockAlertsUseCase
) : ViewModel() {

    val userName: StateFlow<String?> = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val stats: StateFlow<DashboardStats> = stockRepository.observeDashboardStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardStats())

    val lowStock: StateFlow<List<LowStockItem>> = getLowStockAlertsUseCase()
        .map { result -> (result as? Resource.Success)?.data ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
