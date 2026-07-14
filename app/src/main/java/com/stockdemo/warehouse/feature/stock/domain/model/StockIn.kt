package com.stockdemo.warehouse.feature.stock.domain.model

import com.stockdemo.warehouse.feature.auth.domain.model.User

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



