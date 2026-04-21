package com.example.stockdemo.feature.stock.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.core.network.model.BaseResponse
import com.example.stockdemo.feature.stock.data.local.StockLocalDataSource
import com.example.stockdemo.feature.stock.data.mapper.toDomain
import com.example.stockdemo.feature.stock.data.mapper.toEntity
import com.example.stockdemo.feature.stock.data.paging.StockInPagingSource
import com.example.stockdemo.feature.stock.data.paging.StockOutPagingSource
import com.example.stockdemo.feature.stock.data.remote.StockRemoteDataSource
import com.example.stockdemo.feature.stock.data.remote.StockDto
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockIn
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.StockMutationResult
import com.example.stockdemo.feature.stock.domain.model.StockOut
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.feature.stock.sync.StockSyncCoordinator
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class StockRepositoryImpl(
    private val localDataSource: StockLocalDataSource,
    private val remoteDataSource: StockRemoteDataSource,
    private val syncCoordinator: StockSyncCoordinator
) : StockRepository {

    private companion object {
        const val TAG = "StockRepositoryImpl"
    }

    override fun getAllStocks(): Flow<Resource<List<Stock>>> = flow {
        emitLoading()
        val cachedStocks = localDataSource.getCachedStocks()

        if (cachedStocks.isNotEmpty()) {
            emit(Resource.Success(cachedStocks))
        }

        if (!isNetworkAvailable()) {
            if (cachedStocks.isEmpty()) {
                emitError("No cached stock data available offline")
            }
            return@flow
        }

        try {
            val response = remoteDataSource.getAllStocks()
            if (response.success && response.data != null) {
                cacheStockPayload(response.data)
                emit(Resource.Success(response.data.map { it.toDomain() }))
            } else if (cachedStocks.isEmpty()) {
                emitError(response.message ?: "Failed to load stocks")
            }
        } catch (e: Exception) {
            if (cachedStocks.isEmpty()) {
                handleException(e)
            }
        }
    }

    override fun syncMasterProducts(): Flow<Resource<Unit>> = flow {
        emitLoading()
        try {
            val productResponse = remoteDataSource.getProducts()
            val locationResponse = remoteDataSource.getLocations()
            val deliveryOrderResponse = remoteDataSource.getDeliveryOrders()

            if (
                productResponse.success && productResponse.data != null &&
                locationResponse.success && locationResponse.data != null &&
                deliveryOrderResponse.success && deliveryOrderResponse.data != null
            ) {
                localDataSource.replaceProducts(productResponse.data.map { it.toEntity() })
                localDataSource.replaceLocations(locationResponse.data.map { it.toEntity() })
                localDataSource.replaceDeliveryOrders(deliveryOrderResponse.data.map { it.toEntity() })
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
            if (localDataSource.hasProducts()) {
                emit(Resource.Success(Unit))
            } else {
                handleException(e)
            }
        }
    }

    override fun getProductByQrCode(qrCode: String): Flow<Resource<Product>> = flow {
        emitLoading()
        val product = localDataSource.getProductByCodes(productCodeCandidates(qrCode))

        if (product != null) {
            emit(Resource.Success(product))
        } else {
            emitError("Product not found in local master data")
        }
    }

    override fun getStockByQrCode(qrCode: String): Flow<Resource<Stock>> = flow {
        emitLoading()
        emitCachedThenFetch(
            cachedValue = localDataSource.getStockByQrCode(qrCode),
            fetchRemote = { remoteDataSource.getStockByQrCode(qrCode) },
            onRemoteSuccess = { stockDto ->
                cacheStockPayload(listOf(stockDto))
                stockDto.toDomain()
            },
            emptyRemoteMessage = "Stock not found"
        )
    }

    override fun getDeliveryOrderByQrCode(qrCode: String): Flow<Resource<DeliveryOrder>> = flow {
        emitLoading()
        val order = localDataSource.getDeliveryOrderByQrCode(qrCode)
        if (order != null) {
            emit(Resource.Success(order))
        } else {
            emitError("Delivery order not found in local cache")
        }
    }

    override fun stockIn(stockInRequest: StockInRequest): Flow<Resource<StockMutationResult>> = flow {
        emitLoading()
        Log.d(TAG, "stockIn() called for qrCode=${stockInRequest.qrCode}")

        if (isNetworkAvailable()) {
            try {
                Log.d(TAG, "stockIn() network available, calling API directly")
                val response = remoteDataSource.stockIn(stockInRequest)
                if (response.success) {
                    Log.d(TAG, "stockIn() API success, syncing immediately")
                    val syncedStock = response.data?.toDomain() ?: fallbackSyncedStock(stockInRequest)
                    emit(Resource.Success(StockMutationResult.Synced(syncedStock)))
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

        syncCoordinator.queueStockIn(stockInRequest)
        emit(Resource.Success(StockMutationResult.Queued))
    }

    override fun updateQuantity(
        id: Int,
        updateQuantityRequest: UpdateQuantityRequest
    ): Flow<Resource<StockMutationResult>> = flow {
        emitLoading()
        if (isNetworkAvailable()) {
            try {
                val response = remoteDataSource.updateQuantity(id, updateQuantityRequest)
                if (response.success && response.data != null) {
                    cacheStockPayload(listOf(response.data))
                    emit(Resource.Success(StockMutationResult.Synced(response.data.toDomain())))
                    return@flow
                }
            } catch (_: Exception) {
            }
        }

        syncCoordinator.queueStockOut(id, updateQuantityRequest)
        emit(Resource.Success(StockMutationResult.Queued))
    }

    override suspend fun syncPendingStockOuts() {
        syncCoordinator.syncPendingStockOuts()
    }

    override fun getLocationByQrCode(qrCode: String): Flow<Resource<Location>> = flow {
        emitLoading()
        emitCachedThenFetch(
            cachedValue = localDataSource.getLocationByQrCode(qrCode),
            fetchRemote = { remoteDataSource.getLocationByQrCode(qrCode) },
            onRemoteSuccess = { locationDto ->
                localDataSource.cacheLocation(locationDto.toEntity())
                locationDto.toDomain()
            },
            emptyRemoteMessage = "Location not found in local master data"
        )
    }

    override fun getStockInHistory(pageSize: Int): Flow<PagingData<StockIn>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { StockInPagingSource(remoteDataSource) }
        ).flow
    }

    override fun getStockOutHistory(pageSize: Int): Flow<PagingData<StockOut>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = { StockOutPagingSource(remoteDataSource) }
        ).flow
    }

    override suspend fun syncPendingStockIns() {
        syncCoordinator.syncPendingStockIns()
    }

    private fun isNetworkAvailable(): Boolean = syncCoordinator.isNetworkAvailable()

    private suspend fun cacheStockPayload(stocks: List<StockDto>) {
        localDataSource.cacheProducts(stocks.mapNotNull { it.product }.map { it.toEntity() })
        localDataSource.cacheLocations(stocks.mapNotNull { it.location }.map { it.toEntity() })
        localDataSource.cacheStocks(stocks.map { it.toEntity() })
    }

    private fun fallbackSyncedStock(stockInRequest: StockInRequest): Stock = Stock(
        stockId = -1,
        productId = stockInRequest.productId,
        locationId = stockInRequest.locationId,
        quantity = stockInRequest.quantity,
        qrCode = stockInRequest.qrCode,
        lastUpdated = null
    )

    private suspend fun <T> FlowCollector<Resource<T>>.emitLoading() {
        emit(Resource.Loading())
    }

    private suspend fun <T> FlowCollector<Resource<T>>.emitError(message: String) {
        emit(Resource.Error(message))
    }

    private suspend fun <Remote : Any, Domain : Any> FlowCollector<Resource<Domain>>.emitCachedThenFetch(
        cachedValue: Domain?,
        fetchRemote: suspend () -> BaseResponse<Remote>,
        onRemoteSuccess: suspend (Remote) -> Domain,
        emptyRemoteMessage: String
    ) {
        if (cachedValue != null) {
            emit(Resource.Success(cachedValue))
            if (!isNetworkAvailable()) {
                return
            }
        } else if (!isNetworkAvailable()) {
            emitError("Network unavailable")
            return
        }

        try {
            val response = fetchRemote()
            if (response.success && response.data != null) {
                emit(Resource.Success(onRemoteSuccess(response.data)))
            } else if (cachedValue == null) {
                emitError(response.message ?: emptyRemoteMessage)
            }
        } catch (e: Exception) {
            if (cachedValue == null) {
                handleException(e)
            }
        }
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
