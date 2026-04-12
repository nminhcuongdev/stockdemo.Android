package com.example.stockdemo.feature.stock.domain.model

sealed interface StockMutationResult {
    data class Synced(val stock: Stock) : StockMutationResult
    data object Queued : StockMutationResult
}
