package com.stockdemo.warehouse.feature.stock.data.remote

/** An EPC <-> product pairing as returned by the /EpcMappings API. */
data class EpcMappingDto(
    val epc: String,
    val stockId: Int,
    val qrCode: String?,
    val productCode: String?,
    val productName: String?,
    val locationName: String?,
    val quantity: Int,
    val mappedDate: String?
)

/** Request body for POST /EpcMappings. */
data class AssignEpcRequest(
    val epc: String,
    val qrCode: String
)
