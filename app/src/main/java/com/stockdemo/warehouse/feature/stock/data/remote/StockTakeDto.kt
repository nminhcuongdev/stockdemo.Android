package com.stockdemo.warehouse.feature.stock.data.remote

data class StockTakeDto(
    val stockTakeId: Int,
    val locationId: Int,
    val status: String,
    val note: String?,
    val items: List<StockTakeItemDto>?
)

data class StockTakeItemDto(
    val stockTakeItemId: Int,
    val productId: Int,
    val product: ProductDto?,
    val systemQuantity: Int,
    val countedQuantity: Int,
    val variance: Int
)
