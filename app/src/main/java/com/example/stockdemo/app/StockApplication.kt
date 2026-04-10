package com.example.stockdemo.app

import android.app.Application
import com.example.stockdemo.core.ui.util.LanguageManager
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class StockApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val savedLanguageCode = runBlocking {
            UserPreferences(this@StockApplication).getSavedLanguageCode()
        }
        LanguageManager.applyLanguage(savedLanguageCode)
    }
}


