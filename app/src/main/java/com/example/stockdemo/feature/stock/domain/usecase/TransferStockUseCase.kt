package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.TransferStockRequest
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransferStockUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(request: TransferStockRequest): Flow<Resource<Unit>> {
        return repository.transferStock(request)
    }
}
