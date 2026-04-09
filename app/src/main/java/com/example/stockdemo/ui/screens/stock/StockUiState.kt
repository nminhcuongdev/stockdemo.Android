package com.example.stockdemo.ui.screens.stock

import com.example.stockdemo.domain.model.stock.Stock

data class StockUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)