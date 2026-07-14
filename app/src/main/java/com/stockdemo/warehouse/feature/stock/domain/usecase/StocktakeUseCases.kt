package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.core.common.Resource
import com.stockdemo.warehouse.feature.stock.domain.model.CreateStockTakeRequest
import com.stockdemo.warehouse.feature.stock.domain.model.Product
import com.stockdemo.warehouse.feature.stock.domain.model.StockTake
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateStocktakeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(request: CreateStockTakeRequest): Flow<Resource<StockTake>> =
        repository.createStocktake(request)
}

class CompleteStocktakeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(stockTakeId: Int): Flow<Resource<Unit>> =
        repository.completeStocktake(stockTakeId)
}

class GetProductsUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend operator fun invoke(): List<Product> = repository.getCachedProducts()
}
