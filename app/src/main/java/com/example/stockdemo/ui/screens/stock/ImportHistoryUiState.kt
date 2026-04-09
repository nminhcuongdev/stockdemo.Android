package com.example.stockdemo.ui.screens.stock

import com.example.stockdemo.domain.model.stock.StockIn

data class ImportHistoryUiState(
    val isLoading: Boolean = false,
    val items: List<StockIn> = emptyList(),
    val error: String? = null
)