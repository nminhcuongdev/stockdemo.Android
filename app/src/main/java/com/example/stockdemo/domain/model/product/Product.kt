package com.example.stockdemo.domain.model.product

data class Product(
    val productId: Int,
    val productCode: String,
    val productName: String,
    val description: String,
    val unit: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String? = null
)