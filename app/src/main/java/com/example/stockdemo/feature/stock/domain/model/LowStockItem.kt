package com.example.stockdemo.feature.stock.domain.model

data class LowStockItem(
    val productId: Int,
    val productCode: String,
    val productName: String,
    val unit: String,
    val currentQuantity: Int,
    val minQuantity: Int,
    val shortage: Int
)
