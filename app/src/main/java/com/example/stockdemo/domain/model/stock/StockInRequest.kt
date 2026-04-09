package com.example.stockdemo.domain.model.stock

data class StockInRequest(
    val locationId: Int,
    val productId: Int,
    val qrCode: String,
    val quantity: Int,
    val userId: Int
)