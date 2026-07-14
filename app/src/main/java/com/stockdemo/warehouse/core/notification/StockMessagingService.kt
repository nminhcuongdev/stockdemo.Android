package com.stockdemo.warehouse.core.notification

import com.stockdemo.warehouse.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StockMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenManager: NotificationTokenManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch { tokenManager.register(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.notif_channel_low_stock)
        val body = message.notification?.body ?: message.data["body"] ?: ""
        NotificationHelper.show(this, title, body)
    }
}
