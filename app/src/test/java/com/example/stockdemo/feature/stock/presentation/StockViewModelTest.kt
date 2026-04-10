package com.example.stockdemo.feature.stock.presentation

import android.content.Context
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.usecase.GetDeliveryOrderByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetLocationByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStockByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStocksUseCase
import com.example.stockdemo.feature.stock.domain.usecase.StockInUseCase
import com.example.stockdemo.feature.stock.domain.usecase.SyncMasterProductsUseCase
import com.example.stockdemo.feature.stock.domain.usecase.UpdateQuantityUseCase
import com.example.stockdemo.testutil.MainDispatcherRule
import com.example.stockdemo.testutil.mockContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getStocksUseCase: GetStocksUseCase = mockk()
    private val getDeliveryOrderByQrCodeUseCase: GetDeliveryOrderByQrCodeUseCase = mockk()
    private val getLocationByQrCodeUseCase: GetLocationByQrCodeUseCase = mockk()
    private val stockInUseCase: StockInUseCase = mockk()
    private val getStockByQrCodeUseCase: GetStockByQrCodeUseCase = mockk()
    private val updateQuantityUseCase: UpdateQuantityUseCase = mockk()
    private val syncMasterProductsUseCase: SyncMasterProductsUseCase = mockk()

    private val context: Context = mockContext(
        mapOf(
            R.string.product_master_sync_failed to "Product sync failed",
            R.string.toast_delivery_order_not_found to "Delivery order not found",
            R.string.toast_location_not_found to "Location not found",
            R.string.toast_stock_not_found to "Stock not found",
            R.string.toast_sync_server_success to "Synced to server",
            R.string.toast_import_queued to "Import queued",
            R.string.toast_export_queued to "Export queued",
            R.string.toast_import_failed to "Import failed",
            R.string.toast_export_failed to "Export failed",
            R.string.unknown_error to "Unknown error"
        )
    )

    private fun sampleStock(): Stock = Stock(
        stockId = 10,
        productId = 17,
        locationId = 1,
        quantity = 20,
        qrCode = "QR-STOCK-001",
        lastUpdated = "2026-04-10T00:00:00",
        product = Product(
            productId = 17,
            productCode = "PRD001",
            productName = "Carton",
            description = "Carton box",
            unit = "Cai",
            isActive = true,
            createdDate = "2026-04-10T00:00:00"
        ),
        location = Location(
            locationId = 1,
            locationCode = "A-01",
            locationName = "Khu A - Tầng 1",
            isActive = true,
            createdDate = "2026-04-10T00:00:00"
        )
    )

    private fun sampleDeliveryOrder(): DeliveryOrder = DeliveryOrder(
        deliveryOrderId = 1,
        poNumber = "PO-001",
        productId = 17,
        quantity = 20,
        qrCode = "QR-DO-001",
        status = "CREATED",
        deliveryDate = "2026-04-10T00:00:00",
        createdDate = "2026-04-10T00:00:00",
        product = sampleStock().product
    )

    private fun sampleLocation(): Location = sampleStock().location!!

    private fun createViewModel(): StockViewModel {
        every { syncMasterProductsUseCase() } returns flowOf(Resource.Success(Unit))
        every { getStocksUseCase() } returns flowOf(Resource.Success(listOf(sampleStock())))
        every { getDeliveryOrderByQrCodeUseCase(any()) } returns flowOf(Resource.Success(sampleDeliveryOrder()))
        every { getLocationByQrCodeUseCase(any()) } returns flowOf(Resource.Success(sampleLocation()))
        every { getStockByQrCodeUseCase(any()) } returns flowOf(Resource.Success(sampleStock()))
        every { stockInUseCase(any()) } returns flowOf(Resource.Success(sampleStock()))
        every { updateQuantityUseCase(any(), any()) } returns flowOf(Resource.Success(sampleStock()))

        return StockViewModel(
            getStocksUseCase,
            getDeliveryOrderByQrCodeUseCase,
            getLocationByQrCodeUseCase,
            stockInUseCase,
            getStockByQrCodeUseCase,
            updateQuantityUseCase,
            syncMasterProductsUseCase,
            context
        )
    }

    @Test
    fun `init loads stocks and syncs master data`() = runTest {
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.stocks.size)
        assertEquals("PRD001", viewModel.state.value.stocks.first().product?.productCode)
        verify(exactly = 1) { syncMasterProductsUseCase() }
        verify(atLeast = 1) { getStocksUseCase() }
    }

    @Test
    fun `stockIn success emits synced message and refreshes stocks`() = runTest {
        val viewModel = createViewModel()
        val messages = mutableListOf<String>()
        val job = backgroundScope.launch { viewModel.toastMessage.collect { messages.add(it) } }

        advanceUntilIdle()

        viewModel.stockIn(
            StockInRequest(
                locationId = 1,
                productId = 17,
                qrCode = "QR-DO-001",
                quantity = 20,
                userId = 99
            )
        )

        advanceUntilIdle()

        assertTrue(messages.contains("Synced to server"))
        verify(atLeast = 2) { getStocksUseCase() }

        job.cancel()
    }

    @Test
    fun `updateQuantity success with null data emits queued message`() = runTest {
        val viewModel = createViewModel()
        val messages = mutableListOf<String>()
        val job = backgroundScope.launch { viewModel.toastMessage.collect { messages.add(it) } }

        advanceUntilIdle()

        every { updateQuantityUseCase(any(), any()) } returns flowOf(Resource.Success(null))

        viewModel.updateQuantity(
            id = 10,
            updateQuantityRequest = UpdateQuantityRequest(quantity = 5, createdBy = 99)
        )

        advanceUntilIdle()

        assertTrue(messages.contains("Export queued"))
        job.cancel()
    }
}
