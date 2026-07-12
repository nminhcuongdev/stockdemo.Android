package com.example.stockdemo.feature.stock.domain.usecase

import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import javax.inject.Inject

class GetLocationsUseCase @Inject constructor(
    private val repository: StockRepository
) {
    suspend operator fun invoke(): List<Location> = repository.getCachedLocations()
}
