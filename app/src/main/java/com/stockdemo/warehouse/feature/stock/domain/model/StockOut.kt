package com.stockdemo.warehouse.feature.stock.domain.model

import com.stockdemo.warehouse.feature.stock.domain.model.Location
import com.stockdemo.warehouse.feature.auth.domain.model.User

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



