package com.example.stockdemo.core.notification

import android.util.Log
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.data.remote.ApiService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Registers this device's FCM token with the backend so it can receive low-stock pushes. */
@Singleton
class NotificationTokenManager @Inject constructor(
    private val api: ApiService,
    private val userPreferences: UserPreferences
) {
    private companion object {
        const val TAG = "NotificationToken"
    }

    /** Fetches the current FCM token and registers it (call after login). */
    suspend fun registerCurrentToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            register(token)
        } catch (e: Exception) {
            Log.w(TAG, "Could not obtain FCM token: ${e.message}")
        }
    }

    /** Registers a specific token (call from onNewToken). */
    suspend fun register(token: String) {
        try {
            val userId = userPreferences.userId.first()
            val locale = userPreferences.languageCode.first()
            api.registerDeviceToken(
                RegisterDeviceTokenRequest(token = token, userId = userId, platform = "android", locale = locale)
            )
            Log.d(TAG, "Device token registered")
        } catch (e: Exception) {
            // Not fatal (e.g. not logged in yet); will retry on next login/refresh.
            Log.w(TAG, "Register token failed: ${e.message}")
        }
    }
}
