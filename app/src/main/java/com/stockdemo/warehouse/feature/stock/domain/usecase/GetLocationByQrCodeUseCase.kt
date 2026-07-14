package com.stockdemo.warehouse.feature.stock.domain.usecase

import com.stockdemo.warehouse.feature.stock.domain.model.Location
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import com.stockdemo.warehouse.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationByQrCodeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(qrCode: String): Flow<Resource<Location>> {
        return repository.getLocationByQrCode(qrCode)
    }
}


