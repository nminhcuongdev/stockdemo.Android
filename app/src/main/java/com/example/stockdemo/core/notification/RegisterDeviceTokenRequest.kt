package com.example.stockdemo.core.notification

data class RegisterDeviceTokenRequest(
    val token: String,
    val userId: Int?,
    val platform: String = "android"
)
