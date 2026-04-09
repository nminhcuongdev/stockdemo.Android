package com.example.stockdemo.domain.usecase.stock

import com.example.stockdemo.domain.model.stock.Stock
import com.example.stockdemo.domain.repository.StockRepository
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStocksUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): Flow<Resource<List<Stock>>> {
        return repository.getAllStocks()
    }
}