package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateQuantityUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(id: Int, request: UpdateQuantityRequest): Flow<Resource<Stock>> {
        return repository.updateQuantity(id, request)
    }
}



