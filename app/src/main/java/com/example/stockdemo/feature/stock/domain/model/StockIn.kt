package com.example.stockdemo.feature.stock.domain.model

import com.example.stockdemo.feature.auth.domain.model.User

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



