package com.example.stockdemo.feature.stock.data.local

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
    val createdDate: String,
    val updatedDate: String? = null
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val locationId: Int,
    val locationCode: String,
    val locationName: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String? = null
)

@Entity(tableName = "pending_stock_ins")
data class PendingStockInEntity(
    @PrimaryKey(autoGenerate = true) val pendingId: Long = 0,
    val productId: Int,
    val locationId: Int,
    val qrCode: String,
    val quantity: Int,
    val userId: Int,
    val createdAt: Long,
    val syncAttempts: Int = 0,
    val lastError: String? = null
)

@Entity(tableName = "pending_stock_outs")
data class PendingStockOutEntity(
    @PrimaryKey(autoGenerate = true) val pendingId: Long = 0,
    val stockId: Int,
    val quantity: Int,
    val createdBy: Int,
    val createdAt: Long,
    val syncAttempts: Int = 0,
    val lastError: String? = null
)

@Entity(tableName = "delivery_orders")
data class DeliveryOrderEntity(
    @PrimaryKey val deliveryOrderId: Int,
    val productId: Int,
    val poNumber: String,
    val deliveryDate: String,
    val quantity: Int,
    val qrCode: String,
    val createdDate: String,
    val updatedDate: String? = null,
    val status: String
)



