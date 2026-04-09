package com.example.stockdemo.domain.model.stock

data class UpdateQuantityRequest(
    val quantity: Int,
    val createdBy: Int
)