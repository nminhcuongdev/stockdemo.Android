package com.stockdemo.warehouse.feature.stock.domain.repository

import androidx.paging.PagingData
import com.stockdemo.warehouse.feature.stock.domain.model.DashboardStats
import com.stockdemo.warehouse.feature.stock.domain.model.CreateStockTakeRequest
import com.stockdemo.warehouse.feature.stock.domain.model.Location
import com.stockdemo.warehouse.feature.stock.domain.model.LowStockItem
import com.stockdemo.warehouse.feature.stock.domain.model.StockTake
import com.stockdemo.warehouse.feature.stock.domain.model.StockMovementReport
import com.stockdemo.warehouse.feature.stock.domain.model.DeliveryOrder
import com.stockdemo.warehouse.feature.stock.domain.model.Product
import com.stockdemo.warehouse.feature.stock.domain.model.Stock
import com.stockdemo.warehouse.feature.stock.domain.model.StockIn
import com.stockdemo.warehouse.feature.stock.domain.model.StockOut
import com.stockdemo.warehouse.feature.stock.domain.model.StockInRequest
import com.stockdemo.warehouse.feature.stock.domain.model.TransferStockRequest
import com.stockdemo.warehouse.feature.stock.domain.model.StockMutationResult
import com.stockdemo.warehouse.feature.stock.domain.model.UpdateQuantityRequest
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun observeDashboardStats(): Flow<DashboardStats>
    fun getAllStocks(): Flow<Resource<List<Stock>>>
    fun syncMasterProducts(): Flow<Resource<Unit>>
    fun getProductByQrCode(qrCode: String): Flow<Resource<Product>>
    fun getStockByQrCode(qrCode: String): Flow<Resource<Stock>>
    fun getDeliveryOrderByQrCode(qrCode: String): Flow<Resource<DeliveryOrder>>
    fun stockIn(stockInRequest: StockInRequest): Flow<Resource<StockMutationResult>>
    fun updateQuantity(
        id: Int,
        updateQuantityRequest: UpdateQuantityRequest
    ): Flow<Resource<StockMutationResult>>
    fun getLocationByQrCode(qrCode: String): Flow<Resource<Location>>
    fun getStockInHistory(pageSize: Int): Flow<PagingData<StockIn>>
    fun getStockOutHistory(pageSize: Int): Flow<PagingData<StockOut>>
    suspend fun syncPendingStockIns()
    suspend fun syncPendingStockOuts()
    fun transferStock(request: TransferStockRequest): Flow<Resource<Unit>>
    suspend fun getCachedLocations(): List<Location>
    fun getLowStockAlerts(): Flow<Resource<List<LowStockItem>>>
    fun createStocktake(request: CreateStockTakeRequest): Flow<Resource<StockTake>>
    fun completeStocktake(stockTakeId: Int): Flow<Resource<Unit>>
    suspend fun getCachedProducts(): List<Product>
    fun getStockMovementReport(from: String, to: String): Flow<Resource<StockMovementReport>>
}


