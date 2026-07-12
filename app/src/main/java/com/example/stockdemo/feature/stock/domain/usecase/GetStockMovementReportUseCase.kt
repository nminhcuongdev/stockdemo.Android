package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.StockMovementReport
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockMovementReportUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(from: String, to: String): Flow<Resource<StockMovementReport>> =
        repository.getStockMovementReport(from, to)
}
