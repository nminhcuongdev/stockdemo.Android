package com.example.satostockmanagement.models.stocks

data class StockInRequest(
    val locationId: Int,
    val productId: Int,
    val qrCode: String,
    val quantity: Int,
    val userId: Int
)