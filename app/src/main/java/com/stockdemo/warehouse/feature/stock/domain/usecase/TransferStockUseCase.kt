package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.core.common.Resource
import com.stockdemo.warehouse.feature.stock.domain.model.TransferStockRequest
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransferStockUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(request: TransferStockRequest): Flow<Resource<Unit>> {
        return repository.transferStock(request)
    }
}
