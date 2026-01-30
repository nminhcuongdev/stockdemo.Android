package com.example.satostockmanagement.repository

import com.example.satostockmanagement.models.Location
import com.example.satostockmanagement.models.deliveryOrders.DeliveryOrder
import com.example.satostockmanagement.models.stocks.Stock
import com.example.satostockmanagement.models.stocks.StockInRequest
import com.example.satostockmanagement.models.stocks.UpdateQuantityRequest
import com.example.satostockmanagement.retrofit.BaseResponse
import com.example.satostockmanagement.retrofit.RetrofitInstance

class StockRepository {
    private val apiService = RetrofitInstance.api

    suspend fun getAllStocks(): BaseResponse<List<Stock>> {
        return apiService.getAllStocks()
    }

    suspend fun getStockByQrCode(qrCode: String): BaseResponse<Stock> {
        return apiService.getStockByQrCode(qrCode)
    }

    suspend fun getDObyQrCode(qrCode: String): BaseResponse<DeliveryOrder> {
        return apiService.getDObyQrCode(qrCode)
    }

    suspend fun stockIn(stockInRequest: StockInRequest): BaseResponse<Stock> {
        return apiService.stockIn(stockInRequest)
    }

    suspend fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest): BaseResponse<Stock> {
        return apiService.updateQuantity(id, updateQuantityRequest)
    }

    suspend fun getLocationByQrCode(qrCode: String): BaseResponse<Location> {
        return apiService.getLocationByQrCode(qrCode)
    }
}