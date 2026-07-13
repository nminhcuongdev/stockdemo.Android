package com.example.stockdemo.feature.stock.data.remote

import com.example.stockdemo.core.network.model.BaseResponse
import com.example.stockdemo.core.network.model.PagedResponse
import com.example.stockdemo.core.notification.RegisterDeviceTokenRequest
import com.example.stockdemo.feature.auth.data.repository.LoginResponseDto
import com.example.stockdemo.feature.auth.domain.model.LoginRequest
import com.example.stockdemo.feature.stock.domain.model.CreateStockTakeRequest
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.TransferStockRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("Users/login")
    suspend fun login(@Body login: LoginRequest): BaseResponse<LoginResponseDto>

    @GET("Stocks")
    suspend fun getAllStocks(): BaseResponse<List<StockDto>>

    @GET("Products")
    suspend fun getProducts(): BaseResponse<List<ProductDto>>

    @GET("DeliveryOrders")
    suspend fun getDeliveryOrders(): BaseResponse<List<DeliveryOrderDto>>

    @GET("Locations")
    suspend fun getLocations(): BaseResponse<List<LocationDto>>

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

    @POST("StockTransfers")
    suspend fun transferStock(@Body request: TransferStockRequest): BaseResponse<StockTransferDto>

    @GET("StockAlerts/low-stock")
    suspend fun getLowStock(): BaseResponse<List<LowStockItemDto>>

    @POST("StockTakes")
    suspend fun createStocktake(@Body request: CreateStockTakeRequest): BaseResponse<StockTakeDto>

    @POST("StockTakes/{id}/complete")
    suspend fun completeStocktake(@Path("id") id: Int): BaseResponse<StockTakeDto>

    @GET("reports/stock-movement")
    suspend fun getStockMovementReport(
        @Query("from") from: String,
        @Query("to") to: String
    ): BaseResponse<StockMovementReportDto>

    @GET("DeliveryOrders/qrcode/{qrCode}")
    suspend fun getDeliveryOrderByQrCode(
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

    @GET("EpcMappings")
    suspend fun getEpcMappings(): BaseResponse<List<EpcMappingDto>>

    @POST("EpcMappings")
    suspend fun assignEpc(@Body request: AssignEpcRequest): BaseResponse<EpcMappingDto>

    @DELETE("EpcMappings/{epc}")
    suspend fun deleteEpcMapping(@Path("epc", encoded = true) epc: String): BaseResponse<Boolean>

    @POST("Devices/register-token")
    suspend fun registerDeviceToken(@Body request: RegisterDeviceTokenRequest): BaseResponse<Unit>

}


