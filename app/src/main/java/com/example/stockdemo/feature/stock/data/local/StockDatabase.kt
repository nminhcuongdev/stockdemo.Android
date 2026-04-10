package com.example.stockdemo.feature.stock.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StockEntity::class, ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StockDatabase : RoomDatabase() {
    abstract val stockDao: StockDao
}


