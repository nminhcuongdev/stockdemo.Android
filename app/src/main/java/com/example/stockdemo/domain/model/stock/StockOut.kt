package com.example.stockdemo.domain.model.stock

import com.example.stockdemo.domain.model.location.Location
import com.example.stockdemo.domain.model.user.User

data class StockOut(
    val stockOutId: Int,
    val stockOutCode: String?,
    val productId: Int,
    val locationId: Int,
    val quantity: Int,
    val qrCode: String?,
    val createdBy: Int,
    val createdDate: String,
    val product: Product?,
    val location: Location?,
    val user: User?
)
