package com.example.stockdemo.feature.stock.data.remote

/** A product below its reorder level, as returned by GET /StockAlerts/low-stock. */
data class LowStockItemDto(
    val productId: Int,
    val product: ProductDto?,
    val currentQuantity: Int,
    val minQuantity: Int,
    val maxQuantity: Int?,
    val shortage: Int
)
