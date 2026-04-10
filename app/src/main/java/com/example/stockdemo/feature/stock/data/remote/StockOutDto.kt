package com.example.stockdemo.feature.stock.data.remote

import com.example.stockdemo.feature.auth.data.repository.UserDto

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



