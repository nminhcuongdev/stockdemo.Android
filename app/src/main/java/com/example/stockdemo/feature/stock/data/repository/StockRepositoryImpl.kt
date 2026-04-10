package com.example.stockdemo.feature.stock.data.repository

import android.content.Context
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.core.ui.util.NetworkManager
import com.example.stockdemo.feature.stock.data.local.StockDao
import com.example.stockdemo.feature.stock.data.mapper.toDomain
import com.example.stockdemo.feature.stock.data.mapper.toEntity
import com.example.stockdemo.feature.stock.data.mapper.toPendingEntity
import com.example.stockdemo.feature.stock.data.paging.StockInPagingSource
import com.example.stockdemo.feature.stock.data.paging.StockOutPagingSource
import com.example.stockdemo.feature.stock.data.remote.ApiService
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockIn
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.StockOut
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.feature.stock.sync.StockSyncScheduler
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException

class StockRepositoryImpl(
    private val context: Context,
    private val api: ApiService,
    private val stockDao: StockDao
) : StockRepository {

    private companion object {
        const val TAG = "StockRepositoryImpl"
    }

    override fun getAllStocks(): Flow<Resource<List<Stock>>> = flow {
        emit(Resource.Loading())
        val cachedStockEntities = stockDao.getAllStocks()
            .firstOrNull()
            .orEmpty()
        val cachedProductMap = stockDao.getProducts().associateBy { it.productId }
        val cachedLocationMap = stockDao.getLocations().associateBy { it.locationId }
        val cachedStocks = cachedStockEntities.map { entity ->
            entity.toDomain(
                product = cachedProductMap[entity.productId]?.toDomain(),
                location = cachedLocationMap[entity.locationId]?.toDomain()
            )
        }

        if (cachedStocks.isNotEmpty()) {
            emit(Resource.Success(cachedStocks))
        }

        if (!NetworkManager.isNetworkAvailable(context)) {
            if (cachedStocks.isEmpty()) {
                emit(Resource.Error("No cached stock data available offline"))
            }
            return@flow
        }

        try {
            val response = api.getAllStocks()
            if (response.success && response.data != null) {
                response.data.mapNotNull { it.product }.let { products ->
                    if (products.isNotEmpty()) stockDao.insertProducts(products.map { it.toEntity() })
                }
                response.data.mapNotNull { it.location }.forEach { location ->
                    stockDao.insertLocation(location.toEntity())
                }
                stockDao.insertStocks(response.data.map { it.toEntity() })
                emit(Resource.Success(response.data.map { it.toDomain() }))
            } else if (cachedStocks.isEmpty()) {
                emit(Resource.Error(response.message ?: "Failed to load stocks"))
            }
        } catch (e: Exception) {
            if (cachedStocks.isEmpty()) {
                handleException(e)
            }
        }
    }

    override fun syncMasterProducts(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val productResponse = api.getProducts()
            val locationResponse = api.getLocations()
            val deliveryOrderResponse = api.getDeliveryOrders()

            if (
                productResponse.success && productResponse.data != null &&
                locationResponse.success && locationResponse.data != null &&
                deliveryOrderResponse.success && deliveryOrderResponse.data != null
            ) {
                stockDao.clearProducts()
                stockDao.insertProducts(productResponse.data.map { it.toEntity() })

                stockDao.clearLocations()
                stockDao.insertLocations(locationResponse.data.map { it.toEntity() })

                stockDao.clearDeliveryOrders()
                stockDao.insertDeliveryOrders(deliveryOrderResponse.data.map { it.toEntity() })

                emit(Resource.Success(Unit))
            } else {
                emit(
                    Resource.Error(
                        productResponse.message
                            ?: locationResponse.message
                            ?: deliveryOrderResponse.message
                            ?: "Failed to sync master data"
                    )
                )
            }
        } catch (e: Exception) {
            if (stockDao.getProducts().isNotEmpty()) {
                emit(Resource.Success(Unit))
            } else {
                handleException(e)
            }
        }
    }

    override fun getProductByQrCode(qrCode: String): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())
        val product = productCodeCandidates(qrCode)
            .firstNotNullOfOrNull { code -> stockDao.getProductByCode(code) }

        if (product != null) {
            emit(Resource.Success(product.toDomain()))
        } else {
            emit(Resource.Error("Product not found in local master data"))
        }
    }

    override fun getStockByQrCode(qrCode: String): Flow<Resource<Stock>> = flow {
        emit(Resource.Loading())
        val cachedStockEntity = stockDao.getStockByQrCode(qrCode)
        if (cachedStockEntity != null) {
            val cachedProduct = stockDao.getProductById(cachedStockEntity.productId)?.toDomain()
            val cachedLocation = stockDao.getLocations()
                .firstOrNull { it.locationId == cachedStockEntity.locationId }
                ?.toDomain()
            emit(
                Resource.Success(
                    cachedStockEntity.toDomain(
                        product = cachedProduct,
                        location = cachedLocation
                    )
                )
            )
            if (!NetworkManager.isNetworkAvailable(context)) {
                return@flow
            }
        }
        try {
            val response = api.getStockByQrCode(qrCode)
            if (response.success && response.data != null) {
                response.data.product?.let { stockDao.insertProducts(listOf(it.toEntity())) }
                response.data.location?.let { stockDao.insertLocation(it.toEntity()) }
                stockDao.insertStocks(listOf(response.data.toEntity()))
                emit(Resource.Success(response.data.toDomain()))
            } else if (cachedStockEntity == null) {
                emit(Resource.Error(response.message ?: "Stock not found"))
            }
        } catch (e: Exception) {
            if (cachedStockEntity == null) {
                handleException(e)
            }
        }
    }

    override fun getDeliveryOrderByQrCode(qrCode: String): Flow<Resource<DeliveryOrder>> = flow {
        emit(Resource.Loading())
        val order = stockDao.getDeliveryOrderByQrCode(qrCode)
        if (order != null) {
            val product = stockDao.getProductById(order.productId)?.toDomain()
            emit(Resource.Success(order.toDomain(product)))
        } else {
            emit(Resource.Error("Delivery order not found in local cache"))
        }
    }

    override fun stockIn(stockInRequest: StockInRequest): Flow<Resource<Stock>> = flow {
        emit(Resource.Loading())
        Log.d(TAG, "stockIn() called for qrCode=${stockInRequest.qrCode}")

        if (NetworkManager.isNetworkAvailable(context)) {
            try {
                Log.d(TAG, "stockIn() network available, calling API directly")
                val response = api.stockIn(stockInRequest)
                if (response.success) {
                    Log.d(TAG, "stockIn() API success, syncing immediately")
                    val syncedStock = response.data?.toDomain() ?: Stock(
                        stockId = -1,
                        productId = stockInRequest.productId,
                        locationId = stockInRequest.locationId,
                        quantity = stockInRequest.quantity,
                        qrCode = stockInRequest.qrCode,
                        lastUpdated = null
                    )
                    emit(Resource.Success(syncedStock))
                    return@flow
                } else {
                    Log.d(TAG, "stockIn() API returned success=false, fallback to queue")
                }
            } catch (e: Exception) {
                Log.d(TAG, "stockIn() direct API failed, fallback to queue: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "stockIn() network unavailable, storing pending work")
        }

        stockDao.insertPendingStockIn(stockInRequest.toPendingEntity())
        enqueueSyncWork()
        Log.d(TAG, "stockIn() queued locally")
        emit(Resource.Success(null))
    }

    override fun updateQuantity(
        id: Int,
        updateQuantityRequest: UpdateQuantityRequest
    ): Flow<Resource<Stock>> = flow {
        emit(Resource.Loading())
        if (NetworkManager.isNetworkAvailable(context)) {
            try {
                val response = api.updateQuantity(id, updateQuantityRequest)
                if (response.success && response.data != null) {
                    stockDao.insertStocks(listOf(response.data.toEntity()))
                    emit(Resource.Success(response.data.toDomain()))
                    return@flow
                }
            } catch (_: Exception) {
            }
        }

        stockDao.insertPendingStockOut(updateQuantityRequest.toPendingEntity(id))
        enqueueSyncWork()
        emit(Resource.Success(null))
    }

    override suspend fun syncPendingStockOuts() {
        val items = stockDao.getPendingStockOuts()
        Log.d(TAG, "syncPendingStockOuts() started, pendingCount=${items.size}")
        for (item in items) {
            try {
                Log.d(TAG, "syncPendingStockOuts() syncing pendingId=${item.pendingId}")
                val response = api.updateQuantity(
                    item.stockId,
                    UpdateQuantityRequest(
                        quantity = item.quantity,
                        createdBy = item.createdBy
                    )
                )
                if (response.success) {
                    stockDao.deletePendingStockOut(item.pendingId)
                    Log.d(TAG, "syncPendingStockOuts() deleted pendingId=${item.pendingId}")
                } else {
                    stockDao.markPendingStockOutFailed(
                        pendingId = item.pendingId,
                        error = response.message ?: "Sync failed"
                    )
                    Log.d(TAG, "syncPendingStockOuts() server returned success=false for pendingId=${item.pendingId}")
                }
            } catch (e: Exception) {
                stockDao.markPendingStockOutFailed(
                    pendingId = item.pendingId,
                    error = e.message ?: "Sync failed"
                )
                Log.d(TAG, "syncPendingStockOuts() failed for pendingId=${item.pendingId}: ${e.message}", e)
                throw e
            }
        }

        if (stockDao.getPendingStockOuts().isNotEmpty() && NetworkManager.isNetworkAvailable(context)) {
            Log.d(TAG, "syncPendingStockOuts() still has pending items, rescheduling")
            StockSyncScheduler.schedule(context)
        } else {
            Log.d(TAG, "syncPendingStockOuts() finished, no more pending items")
        }
    }

    override fun getLocationByQrCode(qrCode: String): Flow<Resource<Location>> = flow {
        emit(Resource.Loading())
        val cachedLocation = stockDao.getLocationByCode(qrCode)
        if (cachedLocation != null) {
            emit(Resource.Success(cachedLocation.toDomain()))
            if (!NetworkManager.isNetworkAvailable(context)) {
                return@flow
            }
        }
        try {
            val response = api.getLocationByQrCode(qrCode)
            if (response.success && response.data != null) {
                stockDao.insertLocation(response.data.toEntity())
                emit(Resource.Success(response.data.toDomain()))
            } else if (cachedLocation == null) {
                emit(Resource.Error("Location not found in local master data"))
            }
        } catch (e: Exception) {
            if (cachedLocation == null) {
                handleException(e)
            }
        }
    }

    override fun getStockInHistory(pageSize: Int): Flow<PagingData<StockIn>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { StockInPagingSource(api) }
        ).flow
    }

    override fun getStockOutHistory(pageSize: Int): Flow<PagingData<StockOut>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { StockOutPagingSource(api) }
        ).flow
    }

    override suspend fun syncPendingStockIns() {
        val items = stockDao.getPendingStockIns()
        Log.d(TAG, "syncPendingStockIns() started, pendingCount=${items.size}")
        for (item in items) {
            try {
                Log.d(TAG, "syncPendingStockIns() syncing pendingId=${item.pendingId}")
                val response = api.stockIn(
                    StockInRequest(
                        locationId = item.locationId,
                        productId = item.productId,
                        qrCode = item.qrCode,
                        quantity = item.quantity,
                        userId = item.userId
                    )
                )
                if (response.success) {
                    stockDao.deletePendingStockIn(item.pendingId)
                    Log.d(TAG, "syncPendingStockIns() deleted pendingId=${item.pendingId}")
                } else {
                    stockDao.markPendingStockInFailed(
                        pendingId = item.pendingId,
                        error = response.message ?: "Sync failed"
                    )
                    Log.d(TAG, "syncPendingStockIns() server returned success=false for pendingId=${item.pendingId}")
                }
            } catch (e: Exception) {
                stockDao.markPendingStockInFailed(
                    pendingId = item.pendingId,
                    error = e.message ?: "Sync failed"
                )
                Log.d(TAG, "syncPendingStockIns() failed for pendingId=${item.pendingId}: ${e.message}", e)
                throw e
            }
        }

        if (stockDao.getPendingStockIns().isNotEmpty() && NetworkManager.isNetworkAvailable(context)) {
            Log.d(TAG, "syncPendingStockIns() still has pending items, rescheduling")
            StockSyncScheduler.schedule(context)
        } else {
            Log.d(TAG, "syncPendingStockIns() finished, no more pending items")
        }
    }

    private fun enqueueSyncWork() {
        Log.d(TAG, "enqueueSyncWork() called")
        StockSyncScheduler.schedule(context)
    }

    private suspend fun <T> FlowCollector<Resource<T>>.handleException(e: Exception) {
        when (e) {
            is HttpException -> emit(Resource.Error("Server connection error"))
            is IOException -> emit(Resource.Error("Network unavailable"))
            else -> emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }

    private fun productCodeCandidates(qrCode: String): List<String> {
        val trimmed = qrCode.trim()
        val candidates = mutableListOf(trimmed)
        if (trimmed.contains(";")) {
            candidates += trimmed.split(";").map { it.trim() }.filter { it.isNotBlank() }
        }
        return candidates.distinct()
    }
}
