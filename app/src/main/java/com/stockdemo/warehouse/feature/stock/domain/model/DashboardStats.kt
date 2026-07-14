package com.stockdemo.warehouse.feature.stock.domain.model

/** Aggregated, locally-cached numbers shown on the dashboard. */
data class DashboardStats(
    val totalStockItems: Int = 0,
    val totalQuantity: Int = 0,
    val productCount: Int = 0,
    val pendingSyncCount: Int = 0
)
