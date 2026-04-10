package com.example.stockdemo.feature.stock.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        StockEntity::class,
        ProductEntity::class,
        LocationEntity::class,
        PendingStockInEntity::class,
        PendingStockOutEntity::class,
        DeliveryOrderEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract val stockDao: StockDao
}


