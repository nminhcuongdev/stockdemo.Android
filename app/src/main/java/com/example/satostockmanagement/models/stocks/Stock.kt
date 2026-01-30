package com.example.satostockmanagement.models.stocks

import com.example.satostockmanagement.models.Location
import com.example.satostockmanagement.models.Product

data class Stock(
    val lastUpdated: String,
    val location: Location,
    val locationId: Int,
    val product: Product,
    val productId: Int,
    val qrCode: String,
    val quantity: Int,
    val stockId: Int
)