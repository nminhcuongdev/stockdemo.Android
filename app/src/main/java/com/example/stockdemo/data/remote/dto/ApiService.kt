package com.example.stockdemo.data.remote.dto

import com.example.stockdemo.domain.model.auth.LoginRequest
import com.example.stockdemo.domain.model.stock.StockInRequest
import com.example.stockdemo.domain.model.stock.UpdateQuantityRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("Users/login")
    suspend fun login(@Body login: LoginRequest): BaseResponse<UserDto>

    @GET("Stocks")
    suspend fun getAllStocks(): BaseResponse<List<StockDto>>

    @GET("Stocks/qrcode/{qrCode}")
    suspend fun getStockByQrCode(
        @Path("qrCode", encoded = true) qrCode: String
    ): BaseResponse<StockDto>

    @POST("Stocks")
    suspend fun stockIn(@Body stockInRequest: StockInRequest): BaseResponse<StockDto>

    @PUT("Stocks/{id}/quantity")
    suspend fun updateQuantity(
        @Path("id") id: Int,
        @Body updateQuantityRequest: UpdateQuantityRequest
    ): BaseResponse<StockDto>

    @GET("DeliveryOrders/qrcode/{qrCode}")
    suspend fun getDObyQrCode(
        @Path("qrCode", encoded = true) qrCode: String
    ): BaseResponse<DeliveryOrderDto>

    @GET("Locations/code/{code}")
    suspend fun getLocationByQrCode(
        @Path("code", encoded = true) code: String
    ): BaseResponse<LocationDto>

    @GET("StockIns")
    suspend fun getStockInHistory(
        @Query("sortOrder") sortOrder: String = "asc",
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): BaseResponse<PagedResponse<StockInDto>>

    @GET("StockOuts")
    suspend fun getStockOutHistory(
        @Query("sortOrder") sortOrder: String = "asc",
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("pageSize") pageSize: Int = 10
    ): BaseResponse<PagedResponse<StockOutDto>>

}