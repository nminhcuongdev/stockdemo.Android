package com.example.stockdemo.domain.usecase.stock

import com.example.stockdemo.domain.model.location.Location
import com.example.stockdemo.domain.repository.StockRepository
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLocationByQrCodeUseCase @Inject constructor(
    private val repository: StockRepository
) {
    operator fun invoke(qrCode: String): Flow<Resource<Location>> {
        return repository.getLocationByQrCode(qrCode)
    }
}