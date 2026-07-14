package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.feature.stock.domain.model.Stock
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStockByQrCodeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(qrCode: String): Flow<Resource<Stock>> {
        return repository.getStockByQrCode(qrCode)
    }
}



