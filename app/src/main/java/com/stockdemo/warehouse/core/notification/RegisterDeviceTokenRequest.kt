package com.stockdemo.warehouse.core.notification

data class RegisterDeviceTokenRequest(
    val token: String,
    val userId: Int?,
    val platform: String = "android",
    val locale: String
)
