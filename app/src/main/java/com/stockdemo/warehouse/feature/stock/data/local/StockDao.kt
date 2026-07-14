package com.stockdemo.warehouse.feature.stock.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT COUNT(*) FROM stocks")
    fun observeStockCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM stocks")
    fun observeTotalQuantity(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products")
    fun observeProductCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_stock_ins")
    fun observePendingStockInCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_stock_outs")
    fun observePendingStockOutCount(): Flow<Int>

    @Query("SELECT * FROM stocks")
    suspend fun getAllStockEntities(): List<StockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Query("DELETE FROM stocks")
    suspend fun clearStocks()
    
    @Query("SELECT * FROM stocks WHERE qrCode = :qrCode")
    suspend fun getStockByQrCode(qrCode: String): StockEntity?

    @Query("SELECT * FROM products")
    suspend fun getProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("SELECT * FROM products WHERE productCode = :productCode LIMIT 1")
    suspend fun getProductByCode(productCode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("DELETE FROM locations")
    suspend fun clearLocations()

    @Query("SELECT * FROM locations WHERE locationCode = :locationCode LIMIT 1")
    suspend fun getLocationByCode(locationCode: String): LocationEntity?

    @Query("SELECT * FROM locations")
    suspend fun getLocations(): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingStockIn(item: PendingStockInEntity): Long

    @Query("SELECT * FROM pending_stock_ins ORDER BY createdAt ASC")
    suspend fun getPendingStockIns(): List<PendingStockInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingStockOut(item: PendingStockOutEntity): Long

    @Query("SELECT * FROM pending_stock_outs ORDER BY createdAt ASC")
    suspend fun getPendingStockOuts(): List<PendingStockOutEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryOrders(items: List<DeliveryOrderEntity>)

    @Query("DELETE FROM delivery_orders")
    suspend fun clearDeliveryOrders()

    @Query("SELECT * FROM delivery_orders WHERE qrCode = :qrCode LIMIT 1")
    suspend fun getDeliveryOrderByQrCode(qrCode: String): DeliveryOrderEntity?

    @Query("DELETE FROM pending_stock_ins WHERE pendingId = :pendingId")
    suspend fun deletePendingStockIn(pendingId: Long)

    @Query("""
        UPDATE pending_stock_ins
        SET syncAttempts = syncAttempts + 1,
            lastError = :error
        WHERE pendingId = :pendingId
    """)
    suspend fun markPendingStockInFailed(pendingId: Long, error: String)

    @Query("""
        UPDATE pending_stock_outs
        SET syncAttempts = syncAttempts + 1,
            lastError = :error
        WHERE pendingId = :pendingId
    """)
    suspend fun markPendingStockOutFailed(pendingId: Long, error: String)

    @Query("DELETE FROM pending_stock_outs WHERE pendingId = :pendingId")
    suspend fun deletePendingStockOut(pendingId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpcMapping(mapping: EpcMappingEntity)

    @Query("SELECT * FROM epc_mappings")
    fun observeEpcMappings(): Flow<List<EpcMappingEntity>>

    @Query("DELETE FROM epc_mappings WHERE epc = :epc")
    suspend fun deleteEpcMapping(epc: String)
}


