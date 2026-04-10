package com.example.stockdemo.feature.stock.domain.model

data class UpdateQuantityRequest(
    val quantity: Int,
    val createdBy: Int
)


