package com.example.stockdemo.domain.model.stock

data class RFIDTag(
    val epc: String,
    val rssi: Int,
    val count: Int,
    val timestamp: Long
)
