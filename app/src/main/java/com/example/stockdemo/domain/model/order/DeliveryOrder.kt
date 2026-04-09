package com.example.stockdemo.domain.model.order

import com.example.stockdemo.domain.model.product.Product

data class DeliveryOrder(
    val deliveryOrderId: Int,
    val poNumber: String,
    val productId: Int,
    val quantity: Int,
    val qrCode: String,
    val status: String,
    val deliveryDate: String,
    val createdDate: String,
    val updatedDate: String? = null,
    val product: Product? = null
)