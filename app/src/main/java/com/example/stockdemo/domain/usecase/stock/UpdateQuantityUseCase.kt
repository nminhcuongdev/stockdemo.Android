package com.example.stockdemo.domain.usecase.stock

import com.example.stockdemo.domain.model.stock.Stock
import com.example.stockdemo.domain.model.stock.UpdateQuantityRequest
import com.example.stockdemo.domain.repository.StockRepository
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateQuantityUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(id: Int, request: UpdateQuantityRequest): Flow<Resource<Stock>> {
        return repository.updateQuantity(id, request)
    }
}
