package com.example.stockdemo.domain.model.stock

import com.example.stockdemo.domain.model.location.Location
import com.example.stockdemo.domain.model.product.Product

data class Stock(
    val stockId: Int,
    val productId: Int,
    val locationId: Int,
    val quantity: Int,
    val qrCode: String,
    val lastUpdated: String? = null,
    val product: Product? = null,
    val location: Location? = null
)