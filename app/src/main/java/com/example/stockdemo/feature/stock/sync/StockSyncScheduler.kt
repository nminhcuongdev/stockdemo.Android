package com.example.stockdemo.feature.stock.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object StockSyncScheduler {
    private const val TAG = "StockSyncScheduler"

    fun schedule(context: Context) {
        Log.d(TAG, "schedule() called")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<StockInSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            StockInSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )

        Log.d(TAG, "enqueueUniqueWork() done for ${StockInSyncWorker.WORK_NAME}")
    }
}
