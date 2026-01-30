package com.example.satostockmanagement.models.stocks

data class UpdateQuantityRequest(
    val quantity: Int,
    val createdBy: Int
)