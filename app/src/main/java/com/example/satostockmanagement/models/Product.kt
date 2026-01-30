package com.example.satostockmanagement.models

data class Product(
    val createdDate: String,
    val description: String,
    val isActive: Boolean,
    val productCode: String,
    val productId: Int,
    val productName: String,
    val unit: String,
    val updatedDate: Any
)