package com.example.stockdemo.data.remote.dto

data class StockDto(
    val stockId: Int,
    val productId: Int,
    val locationId: Int,
    val quantity: Int,
    val qrCode: String,
    val lastUpdated: String,
    val product: ProductDto?,
    val location: LocationDto?
)

data class ProductDto(
    val productId: Int,
    val productCode: String,
    val productName: String,
    val description: String,
    val unit: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String?
)

data class LocationDto(
    val locationId: Int,
    val locationCode: String,
    val locationName: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String?
)

data class DeliveryOrderDto(
    val deliveryOrderId: Int,
    val poNumber: String,
    val productId: Int,
    val quantity: Int,
    val qrCode: String,
    val status: String,
    val deliveryDate: String,
    val createdDate: String,
    val updatedDate: String?,
    val product: ProductDto?
)