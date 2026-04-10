package com.example.stockdemo.feature.stock.presentation

import com.example.stockdemo.feature.stock.domain.model.StockIn

data class ImportHistoryUiState(
    val isLoading: Boolean = false,
    val items: List<StockIn> = emptyList(),
    val error: String? = null
)


