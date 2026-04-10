package com.example.stockdemo.feature.stock.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import retrofit2.HttpException

@HiltWorker
class StockInSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: StockRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "StockInSyncWorker"
        const val WORK_NAME = "stock_in_sync_work"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() started")
        return try {
            repository.syncPendingStockIns()
            repository.syncPendingStockOuts()
            Log.d(TAG, "doWork() finished with success")
            Result.success()
        } catch (_: IOException) {
            Log.d(TAG, "doWork() retry because of IOException")
            Result.retry()
        } catch (_: HttpException) {
            Log.d(TAG, "doWork() retry because of HttpException")
            Result.retry()
        } catch (_: Exception) {
            Log.d(TAG, "doWork() failed with unknown exception")
            Result.failure()
        }
    }
}
