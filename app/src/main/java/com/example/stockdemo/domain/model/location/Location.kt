package com.example.stockdemo.domain.model.location

data class Location(
    val locationId: Int,
    val locationCode: String,
    val locationName: String,
    val isActive: Boolean,
    val createdDate: String,
    val updatedDate: String? = null
)