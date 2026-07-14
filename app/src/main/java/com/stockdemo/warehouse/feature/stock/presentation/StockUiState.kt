package com.stockdemo.warehouse.feature.stock.presentation

import com.stockdemo.warehouse.core.ui.UiText
import com.stockdemo.warehouse.feature.stock.domain.model.Stock

data class StockUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null
)


