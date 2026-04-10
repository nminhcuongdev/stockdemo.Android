package com.example.stockdemo.feature.stock.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Query("DELETE FROM stocks")
    suspend fun clearStocks()
    
    @Query("SELECT * FROM stocks WHERE qrCode = :qrCode")
    suspend fun getStockByQrCode(qrCode: String): StockEntity?
}


