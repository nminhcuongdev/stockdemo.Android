package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncMasterProductsUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return repository.syncMasterProducts()
    }
}
