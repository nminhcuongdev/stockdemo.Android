package com.example.stockdemo.feature.stock.presentation

import com.example.stockdemo.core.ui.UiText
import com.example.stockdemo.feature.stock.domain.model.Stock

data class StockUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null
)


