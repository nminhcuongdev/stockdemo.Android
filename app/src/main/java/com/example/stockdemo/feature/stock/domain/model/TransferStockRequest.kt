package com.example.stockdemo.feature.stock.domain.model

data class TransferStockRequest(
    val sourceStockId: Int,
    val toLocationId: Int,
    val quantity: Int,
    val createdBy: Int
)
