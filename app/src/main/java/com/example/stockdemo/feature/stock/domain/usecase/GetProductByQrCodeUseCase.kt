package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductByQrCodeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(qrCode: String): Flow<Resource<Product>> {
        return repository.getProductByQrCode(qrCode)
    }
}
