package com.example.stockdemo.feature.stock.domain.model

data class StockInRequest(
    val locationId: Int,
    val productId: Int,
    val qrCode: String,
    val quantity: Int,
    val userId: Int
)


