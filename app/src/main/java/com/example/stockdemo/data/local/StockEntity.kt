package com.example.stockdemo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey val stockId: Int,
    val productId: Int,
    val locationId: Int,
    val quantity: Int,
    val qrCode: String,
    val lastUpdated: String
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: Int,
    val productCode: String,
    val productName: String,
    val description: String,
    val unit: String,
    val isActive: Boolean,
    val createdDate: String
)
