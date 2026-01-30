package com.example.satostockmanagement.models.deliveryOrders

import com.example.satostockmanagement.models.Product

data class DeliveryOrder(
    val createdDate: String,
    val deliveryDate: String,
    val deliveryOrderId: Int,
    val poNumber: String,
    val product: Product,
    val productId: Int,
    val qrCode: String,
    val quantity: Int,
    val status: String,
    val updatedDate: Any
)