package com.example.stockdemo.feature.stock.data.remote

import com.example.stockdemo.core.network.model.BaseResponse
import com.example.stockdemo.core.network.model.PagedResponse
import com.example.stockdemo.feature.stock.domain.model.CreateStockTakeRequest
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.TransferStockRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRemoteDataSource @Inject constructor(
    private val api: ApiService
) {

    suspend fun getAllStocks(): BaseResponse<List<StockDto>> = api.getAllStocks()

    suspend fun getProducts(): BaseResponse<List<ProductDto>> = api.getProducts()

    suspend fun getLocations(): BaseResponse<List<LocationDto>> = api.getLocations()

    suspend fun getDeliveryOrders(): BaseResponse<List<DeliveryOrderDto>> = api.getDeliveryOrders()

    suspend fun getStockByQrCode(qrCode: String): BaseResponse<StockDto> = api.getStockByQrCode(qrCode)

    suspend fun stockIn(request: StockInRequest): BaseResponse<StockDto> = api.stockIn(request)

    suspend fun updateQuantity(
        id: Int,
        request: UpdateQuantityRequest
    ): BaseResponse<StockDto> = api.updateQuantity(id, request)

    suspend fun transferStock(request: TransferStockRequest): BaseResponse<StockTransferDto> =
        api.transferStock(request)

    suspend fun getLowStock(): BaseResponse<List<LowStockItemDto>> = api.getLowStock()

    suspend fun createStocktake(request: CreateStockTakeRequest): BaseResponse<StockTakeDto> =
        api.createStocktake(request)

    suspend fun completeStocktake(id: Int): BaseResponse<StockTakeDto> = api.completeStocktake(id)

    suspend fun getStockMovementReport(from: String, to: String): BaseResponse<StockMovementReportDto> =
        api.getStockMovementReport(from, to)

    suspend fun getLocationByQrCode(qrCode: String): BaseResponse<LocationDto> {
        return api.getLocationByQrCode(qrCode)
    }

    suspend fun getStockInHistory(
        pageNumber: Int,
        pageSize: Int
    ): BaseResponse<PagedResponse<StockInDto>> {
        return api.getStockInHistory(pageNumber = pageNumber, pageSize = pageSize)
    }

    suspend fun getStockOutHistory(
        pageNumber: Int,
        pageSize: Int
    ): BaseResponse<PagedResponse<StockOutDto>> {
        return api.getStockOutHistory(pageNumber = pageNumber, pageSize = pageSize)
    }

    suspend fun getEpcMappings(): BaseResponse<List<EpcMappingDto>> = api.getEpcMappings()

    suspend fun assignEpc(epc: String, qrCode: String): BaseResponse<EpcMappingDto> =
        api.assignEpc(AssignEpcRequest(epc, qrCode))

    suspend fun deleteEpcMapping(epc: String): BaseResponse<Boolean> = api.deleteEpcMapping(epc)
}
