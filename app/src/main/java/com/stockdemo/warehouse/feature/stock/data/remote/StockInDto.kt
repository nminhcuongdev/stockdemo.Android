package com.stockdemo.warehouse.feature.stock.data.remote

import com.stockdemo.warehouse.feature.auth.data.repository.UserDto

data class StockInDto(
    val stockInId: Int,
    val stockInCode: String?,
    val productId: Int,
    val product: ProductDto?,
    val locationId: Int,
    val location: LocationDto?,
    val quantity: Int,
    val qrCode: String?,
    val createdBy: Int,
    val user: UserDto?,
    val createdDate: String
)


