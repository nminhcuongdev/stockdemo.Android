package com.stockdemo.warehouse.feature.stock.domain.model

data class RFIDTag(
    val epc: String,
    val rssi: Int,
    val count: Int,
    val timestamp: Long
)



