package com.example.stockdemo.domain.model.stock

import com.example.stockdemo.domain.model.location.Location
import com.example.stockdemo.domain.model.user.User

data class StockIn(
    val stockInId: Int,
    val stockInCode: String?,
    val productId: Int,
    val product: Product?,
    val locationId: Int,
    val location: Location?,
    val quantity: Int,
    val qrCode: String?,
    val createdBy: Int,
    val user: User?,
    val createdDate: String
)

data class Product(
    val productId: Int,
    val productCode: String,
    val productName: String,
    val description: String,
    val unit: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String?
)
