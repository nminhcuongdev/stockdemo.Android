package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.core.common.Resource
import com.stockdemo.warehouse.feature.stock.domain.model.LowStockItem
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLowStockAlertsUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): Flow<Resource<List<LowStockItem>>> = repository.getLowStockAlerts()
}
