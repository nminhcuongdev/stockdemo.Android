package com.stockdemo.warehouse.feature.stock.domain.model

data class UpdateQuantityRequest(
    val quantity: Int,
    val createdBy: Int
)


