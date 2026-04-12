package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.StockMutationResult
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StockInUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(request: StockInRequest): Flow<Resource<StockMutationResult>> {
        return repository.stockIn(request)
    }
}



