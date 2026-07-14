package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.feature.stock.domain.model.StockInRequest
import com.stockdemo.warehouse.feature.stock.domain.model.StockMutationResult
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StockInUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(request: StockInRequest): Flow<Resource<StockMutationResult>> {
        return repository.stockIn(request)
    }
}



