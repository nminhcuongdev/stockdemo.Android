package com.stockdemo.warehouse.feature.stock.data.remote

import com.stockdemo.warehouse.feature.auth.data.repository.UserDto

data class StockOutDto(
    val stockOutId: Int,
    val stockOutCode: String?,
    val productId: Int,
    val locationId: Int,
    val quantity: Int,
    val qrCode: String?,
    val createdBy: Int,
    val createdDate: String,
    val product: ProductDto?,
    val location: LocationDto?,
    val user: UserDto?
)



