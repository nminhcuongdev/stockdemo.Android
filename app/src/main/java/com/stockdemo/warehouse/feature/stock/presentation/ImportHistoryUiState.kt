package com.stockdemo.warehouse.feature.stock.presentation

import com.stockdemo.warehouse.feature.stock.domain.model.StockIn

data class ImportHistoryUiState(
    val isLoading: Boolean = false,
    val items: List<StockIn> = emptyList(),
    val error: String? = null
)


