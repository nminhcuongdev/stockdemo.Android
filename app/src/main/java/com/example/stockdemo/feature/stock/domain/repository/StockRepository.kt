package com.example.stockdemo.feature.stock.domain.repository

import androidx.paging.PagingData
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockIn
import com.example.stockdemo.feature.stock.domain.model.StockOut
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getAllStocks(): Flow<Resource<List<Stock>>>
    fun getStockByQrCode(qrCode: String): Flow<Resource<Stock>>
    fun getDeliveryOrderByQrCode(qrCode: String): Flow<Resource<DeliveryOrder>>
    fun stockIn(stockInRequest: StockInRequest): Flow<Resource<Stock>>
    fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest): Flow<Resource<Stock>>
    fun getLocationByQrCode(qrCode: String): Flow<Resource<Location>>
    fun getStockInHistory(pageSize: Int): Flow<PagingData<StockIn>>
    fun getStockOutHistory(pageSize: Int): Flow<PagingData<StockOut>>
}


