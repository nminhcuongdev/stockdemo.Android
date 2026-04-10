package com.example.stockdemo.core.network.model

data class PagedResponse<T>(
    val items: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalCount: Int,
    val totalPages: Int
)


