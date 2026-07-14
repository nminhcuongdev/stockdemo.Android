package com.stockdemo.warehouse.feature.stock.data.remote

data class StockMovementReportDto(
    val from: String?,
    val to: String?,
    val totalIn: Int,
    val totalOut: Int,
    val totalStock: Int,
    val items: List<StockMovementReportItemDto>?
)

data class StockMovementReportItemDto(
    val productId: Int,
    val product: ProductDto?,
    val totalIn: Int,
    val totalOut: Int,
    val currentStock: Int
)
