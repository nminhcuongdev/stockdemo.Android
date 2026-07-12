package com.example.stockdemo.feature.stock.data.remote

/** Response payload for a completed stock transfer. Only the fields the client needs are declared. */
data class StockTransferDto(
    val stockTransferId: Int,
    val productId: Int,
    val fromLocationId: Int,
    val toLocationId: Int,
    val quantity: Int,
    val qrCode: String,
    val createdBy: Int,
    val createdDate: String
)
