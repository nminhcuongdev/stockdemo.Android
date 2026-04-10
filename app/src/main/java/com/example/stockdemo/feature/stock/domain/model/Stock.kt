package com.example.stockdemo.feature.stock.domain.model

import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product

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


