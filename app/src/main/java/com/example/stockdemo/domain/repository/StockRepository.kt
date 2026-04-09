package com.example.stockdemo.domain.repository

import androidx.paging.PagingData
import com.example.stockdemo.domain.model.location.Location
import com.example.stockdemo.domain.model.order.DeliveryOrder
import com.example.stockdemo.domain.model.stock.Stock
import com.example.stockdemo.domain.model.stock.StockIn
import com.example.stockdemo.domain.model.stock.StockOut
import com.example.stockdemo.domain.model.stock.StockInRequest
import com.example.stockdemo.domain.model.stock.UpdateQuantityRequest
import com.example.stockdemo.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun getAllStocks(): Flow<Resource<List<Stock>>>
    fun getStockByQrCode(qrCode: String): Flow<Resource<Stock>>
    fun getDOByQrCode(qrCode: String): Flow<Resource<DeliveryOrder>>
    fun stockIn(stockInRequest: StockInRequest): Flow<Resource<Stock>>
    fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest): Flow<Resource<Stock>>
    fun getLocationByQrCode(qrCode: String): Flow<Resource<Location>>
    fun getStockInHistory(pageSize: Int): Flow<PagingData<StockIn>>
    fun getStockOutHistory(pageSize: Int): Flow<PagingData<StockOut>>
}