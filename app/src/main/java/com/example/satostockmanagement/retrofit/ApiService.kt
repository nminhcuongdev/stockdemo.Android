package com.example.satostockmanagement.retrofit

import com.example.satostockmanagement.models.Location
import com.example.satostockmanagement.models.deliveryOrders.DeliveryOrder
import com.example.satostockmanagement.models.stocks.Stock
import com.example.satostockmanagement.models.stocks.StockInRequest
import com.example.satostockmanagement.models.stocks.UpdateQuantityRequest
import com.example.satostockmanagement.models.users.Login
import com.example.satostockmanagement.models.users.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("Users/login")
    suspend fun login(@Body login: Login): BaseResponse<User>

    @GET("Stocks")
    suspend fun getAllStocks(): BaseResponse<List<Stock>>

    @GET("Stocks/qrcode/{qrCode}")
    suspend fun getStockByQrCode(
        @Path("qrCode", encoded = true) qrCode: String
    ): BaseResponse<Stock>

    @POST("Stocks")
    suspend fun stockIn(@Body stockInRequest: StockInRequest): BaseResponse<Stock>

    @PUT("Stocks/{id}/quantity")
    suspend fun updateQuantity(
        @Path("id") id: Int,
        @Body updateQuantityRequest: UpdateQuantityRequest
    ): BaseResponse<Stock>

    @GET("DeliveryOrders/qrcode/{qrCode}")
    suspend fun getDObyQrCode(
        @Path("qrCode", encoded = true) qrCode: String
    ): BaseResponse<DeliveryOrder>

    @GET("Locations/code/{code}")
    suspend fun getLocationByQrCode(
        @Path("code", encoded = true) code: String
    ): BaseResponse<Location>

}