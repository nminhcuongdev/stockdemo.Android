package com.example.stockdemo.feature.stock.domain.model

data class StockMovementReportItem(
    val productId: Int,
    val productCode: String,
    val productName: String,
    val unit: String,
    val totalIn: Int,
    val totalOut: Int,
    val currentStock: Int
)

data class StockMovementReport(
    val totalIn: Int,
    val totalOut: Int,
    val totalStock: Int,
    val items: List<StockMovementReportItem>
)
