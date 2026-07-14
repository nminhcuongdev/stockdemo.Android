package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.feature.stock.domain.model.StockMutationResult
import com.stockdemo.warehouse.feature.stock.domain.model.UpdateQuantityRequest
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateQuantityUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(id: Int, request: UpdateQuantityRequest): Flow<Resource<StockMutationResult>> {
        return repository.updateQuantity(id, request)
    }
}



