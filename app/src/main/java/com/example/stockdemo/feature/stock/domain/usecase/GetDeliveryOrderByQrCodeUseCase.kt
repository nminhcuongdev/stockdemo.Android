package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeliveryOrderByQrCodeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(qrCode: String): Flow<Resource<DeliveryOrder>> {
        return repository.getDeliveryOrderByQrCode(qrCode)
    }
}


