package com.example.stockdemo.feature.stock.domain.model

data class Location(
    val locationId: Int,
    val locationCode: String,
    val locationName: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String? = null
)


