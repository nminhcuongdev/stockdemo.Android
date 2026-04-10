package com.example.stockdemo.feature.stock.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.stockdemo.feature.stock.data.mapper.toDomain
import com.example.stockdemo.feature.stock.data.paging.StockInPagingSource
import com.example.stockdemo.feature.stock.data.paging.StockOutPagingSource
import com.example.stockdemo.feature.stock.data.remote.ApiService
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockIn
import com.example.stockdemo.feature.stock.domain.model.StockOut
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import com.example.stockdemo.core.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class StockRepositoryImpl(
    private val api: ApiService
) : StockRepository {

    override fun getAllStocks(): Flow<Resource<List<Stock>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getAllStocks()
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.map { it.toDomain() }))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun getStockByQrCode(qrCode: String): Flow<Resource<Stock>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getStockByQrCode(qrCode)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toDomain()))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun getDeliveryOrderByQrCode(qrCode: String): Flow<Resource<DeliveryOrder>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getDeliveryOrderByQrCode(qrCode)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toDomain()))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun stockIn(stockInRequest: StockInRequest): Flow<Resource<Stock>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.stockIn(stockInRequest)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toDomain()))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun updateQuantity(
        id: Int,
        updateQuantityRequest: UpdateQuantityRequest
    ): Flow<Resource<Stock>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.updateQuantity(id, updateQuantityRequest)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toDomain()))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun getLocationByQrCode(qrCode: String): Flow<Resource<Location>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getLocationByQrCode(qrCode)
            if (response.success && response.data != null) {
                emit(Resource.Success(response.data.toDomain()))
            } else {
                emit(Resource.Error(response.message))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override fun getStockInHistory(pageSize: Int): Flow<PagingData<StockIn>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StockInPagingSource(api) }
        ).flow
    }

    override fun getStockOutHistory(pageSize: Int): Flow<PagingData<StockOut>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StockOutPagingSource(api) }
        ).flow
    }

    private suspend fun <T> FlowCollector<Resource<T>>.handleException(e: Exception) {
        when (e) {
            is HttpException -> emit(Resource.Error("Lỗi kết nối server"))
            is IOException -> emit(Resource.Error("Lỗi mạng"))
            else -> emit(Resource.Error(e.message ?: "Lỗi không xác định"))
        }
    }
}



