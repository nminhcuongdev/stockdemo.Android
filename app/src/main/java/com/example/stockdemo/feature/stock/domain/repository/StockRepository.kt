package com.example.stockdemo.feature.stock.domain.repository

import androidx.paging.PagingData
import com.example.stockdemo.feature.stock.domain.model.DashboardStats
import com.example.stockdemo.feature.stock.domain.model.CreateStockTakeRequest
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.LowStockItem
import com.example.stockdemo.feature.stock.domain.model.StockTake
import com.example.stockdemo.feature.stock.domain.model.StockMovementReport
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockIn
import com.example.stockdemo.feature.stock.domain.model.StockOut
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.TransferStockRequest
import com.example.stockdemo.feature.stock.domain.model.StockMutationResult
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.core.common.Resource
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


