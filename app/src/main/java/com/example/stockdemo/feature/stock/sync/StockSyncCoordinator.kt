package com.example.stockdemo.feature.stock.sync

import android.content.Context
import android.util.Log
import com.example.stockdemo.core.ui.util.NetworkManager
import com.example.stockdemo.feature.stock.data.local.StockLocalDataSource
import com.example.stockdemo.feature.stock.data.mapper.toPendingEntity
import com.example.stockdemo.feature.stock.data.remote.StockRemoteDataSource
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockSyncCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localDataSource: StockLocalDataSource,
    private val remoteDataSource: StockRemoteDataSource
) {

    private companion object {
        const val TAG = "StockSyncCoordinator"
    }

    fun isNetworkAvailable(): Boolean = NetworkManager.isNetworkAvailable(context)

    fun enqueueSyncWork() {
        Log.d(TAG, "enqueueSyncWork() called")
        StockSyncScheduler.schedule(context)
    }

    suspend fun queueStockIn(request: StockInRequest) {
        localDataSource.insertPendingStockIn(request.toPendingEntity())
        enqueueSyncWork()
        Log.d(TAG, "queueStockIn() queued locally")
    }

    suspend fun queueStockOut(stockId: Int, request: UpdateQuantityRequest) {
        localDataSource.insertPendingStockOut(request.toPendingEntity(stockId))
        enqueueSyncWork()
        Log.d(TAG, "queueStockOut() queued locally")
    }

    suspend fun syncPendingStockIns() {
        val items = localDataSource.getPendingStockIns()
        Log.d(TAG, "syncPendingStockIns() started, pendingCount=${items.size}")

        for (item in items) {
            try {
                Log.d(TAG, "syncPendingStockIns() syncing pendingId=${item.pendingId}")
                val response = remoteDataSource.stockIn(
                    StockInRequest(
                        locationId = item.locationId,
                        productId = item.productId,
                        qrCode = item.qrCode,
                        quantity = item.quantity,
                        userId = item.userId
                    )
                )
                if (response.success) {
                    localDataSource.deletePendingStockIn(item.pendingId)
                    Log.d(TAG, "syncPendingStockIns() deleted pendingId=${item.pendingId}")
                } else {
                    localDataSource.markPendingStockInFailed(
                        pendingId = item.pendingId,
                        error = response.message ?: "Sync failed"
                    )
                    Log.d(TAG, "syncPendingStockIns() server returned success=false for pendingId=${item.pendingId}")
                }
            } catch (e: Exception) {
                localDataSource.markPendingStockInFailed(
                    pendingId = item.pendingId,
                    error = e.message ?: "Sync failed"
                )
                Log.d(TAG, "syncPendingStockIns() failed for pendingId=${item.pendingId}: ${e.message}", e)
                throw e
            }
        }

        if (localDataSource.getPendingStockIns().isNotEmpty() && isNetworkAvailable()) {
            Log.d(TAG, "syncPendingStockIns() still has pending items, rescheduling")
            StockSyncScheduler.schedule(context)
        } else {
            Log.d(TAG, "syncPendingStockIns() finished, no more pending items")
        }
    }

    suspend fun syncPendingStockOuts() {
        val items = localDataSource.getPendingStockOuts()
        Log.d(TAG, "syncPendingStockOuts() started, pendingCount=${items.size}")

        for (item in items) {
            try {
                Log.d(TAG, "syncPendingStockOuts() syncing pendingId=${item.pendingId}")
                val response = remoteDataSource.updateQuantity(
                    id = item.stockId,
                    request = UpdateQuantityRequest(
                        quantity = item.quantity,
                        createdBy = item.createdBy
                    )
                )
                if (response.success) {
                    localDataSource.deletePendingStockOut(item.pendingId)
                    Log.d(TAG, "syncPendingStockOuts() deleted pendingId=${item.pendingId}")
                } else {
                    localDataSource.markPendingStockOutFailed(
                        pendingId = item.pendingId,
                        error = response.message ?: "Sync failed"
                    )
                    Log.d(TAG, "syncPendingStockOuts() server returned success=false for pendingId=${item.pendingId}")
                }
            } catch (e: Exception) {
                localDataSource.markPendingStockOutFailed(
                    pendingId = item.pendingId,
                    error = e.message ?: "Sync failed"
                )
                Log.d(TAG, "syncPendingStockOuts() failed for pendingId=${item.pendingId}: ${e.message}", e)
                throw e
            }
        }

        if (localDataSource.getPendingStockOuts().isNotEmpty() && isNetworkAvailable()) {
            Log.d(TAG, "syncPendingStockOuts() still has pending items, rescheduling")
            StockSyncScheduler.schedule(context)
        } else {
            Log.d(TAG, "syncPendingStockOuts() finished, no more pending items")
        }
    }
}
