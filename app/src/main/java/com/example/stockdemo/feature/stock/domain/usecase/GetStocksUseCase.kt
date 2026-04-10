package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStocksUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): Flow<Resource<List<Stock>>> {
        return repository.getAllStocks()
    }
}


