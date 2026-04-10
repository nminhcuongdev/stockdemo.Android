package com.example.stockdemo.app

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.stockdemo.core.ui.util.LanguageManager
import com.example.stockdemo.core.ui.util.NetworkManager
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.sync.StockSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class StockApplication : Application(), Configuration.Provider {

    private companion object {
        const val TAG = "StockApplication"
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available callback triggered")
            StockSyncScheduler.schedule(this@StockApplication)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        WorkManager.initialize(this, workManagerConfiguration)

        val savedLanguageCode = runBlocking {
            UserPreferences(this@StockApplication).getSavedLanguageCode()
        }
        LanguageManager.applyLanguage(savedLanguageCode)
        Log.d(TAG, "Application started, language=$savedLanguageCode")

        if (NetworkManager.isNetworkAvailable(this)) {
            Log.d(TAG, "Network available on app start, scheduling sync")
            StockSyncScheduler.schedule(this)
        }

        val connectivityManager =
            getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            Log.d(TAG, "Registering default network callback")
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            Log.d(TAG, "ConnectivityManager is null, cannot register network callback")
        }
    }
}
