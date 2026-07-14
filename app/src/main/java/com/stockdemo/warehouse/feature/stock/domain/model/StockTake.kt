package com.stockdemo.warehouse.feature.stock.domain.model

data class StockTakeCountLine(
    val productId: Int,
    val countedQuantity: Int
)

data class CreateStockTakeRequest(
    val locationId: Int,
    val note: String?,
    val createdBy: Int,
    val items: List<StockTakeCountLine>
)

data class StockTakeItem(
    val productId: Int,
    val productName: String,
    val systemQuantity: Int,
    val countedQuantity: Int,
    val variance: Int
)

data class StockTake(
    val stockTakeId: Int,
    val status: String,
    val items: List<StockTakeItem>
)
