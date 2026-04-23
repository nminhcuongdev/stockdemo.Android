package com.example.stockdemo.feature.stock.presentation

import android.content.Context
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.StockMutationResult
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
            R.string.toast_import_success to "Import success",
            R.string.toast_export_success to "Export success",
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
            locationName = "Tang 1",
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

    private fun sampleStockInRequest(): StockInRequest = StockInRequest(
        locationId = 1,
        productId = 17,
        qrCode = "QR-DO-001",
        quantity = 20,
        userId = 99
    )

    private fun sampleUpdateQuantityRequest(): UpdateQuantityRequest =
        UpdateQuantityRequest(quantity = 5, createdBy = 99)

    private fun stubDefaultUseCases() {
        every { syncMasterProductsUseCase() } returns flowOf(Resource.Success(Unit))
        every { getStocksUseCase() } returns flowOf(Resource.Success(listOf(sampleStock())))
        every { getDeliveryOrderByQrCodeUseCase(any()) } returns flowOf(Resource.Success(sampleDeliveryOrder()))
        every { getLocationByQrCodeUseCase(any()) } returns flowOf(Resource.Success(sampleLocation()))
        every { getStockByQrCodeUseCase(any()) } returns flowOf(Resource.Success(sampleStock()))
        every { stockInUseCase(any()) } returns flowOf(Resource.Success(StockMutationResult.Synced(sampleStock())))
        every { updateQuantityUseCase(any(), any()) } returns flowOf(Resource.Success(StockMutationResult.Synced(sampleStock())))
    }

    private fun createViewModel(): StockViewModel {
        return StockViewModel(
            getStocksUseCase,
            getDeliveryOrderByQrCodeUseCase,
            getLocationByQrCodeUseCase,
            stockInUseCase,
            getStockByQrCodeUseCase,
            updateQuantityUseCase,
            syncMasterProductsUseCase
        )
    }

    private suspend fun awaitNextToast(
        viewModel: StockViewModel,
        scope: CoroutineScope,
        action: () -> Unit
    ): String {
        val deferred = scope.async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.toastMessage.first().asString(context)
        }
        action()
        return deferred.await()
    }

    @Test
    fun `init loads stocks and syncs master data`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(1, viewModel.state.value.stocks.size)
        assertEquals("PRD001", viewModel.state.value.stocks.first().product?.productCode)
        assertNull(viewModel.state.value.error)
        verify(exactly = 1) { syncMasterProductsUseCase() }
        verify(atLeast = 1) { getStocksUseCase() }
    }

    @Test
    fun `syncMasterProducts error emits fallback toast`() = runTest {
        stubDefaultUseCases()
        every { syncMasterProductsUseCase() } returns flowOf(Resource.Error(""))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.syncMasterProducts()
        }

        assertEquals("Product sync failed", message)
    }

    @Test
    fun `getStocks loading and error update state`() = runTest {
        stubDefaultUseCases()
        every { getStocksUseCase() } returns flow {
            emit(Resource.Loading(true))
            emit(Resource.Error("Load failed"))
        }

        val viewModel = createViewModel()

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Load failed", viewModel.state.value.error?.asString(context))
        assertTrue(viewModel.state.value.stocks.isEmpty())
    }

    @Test
    fun `getDeliveryOrderByQrCode success updates scanned delivery order`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.getDeliveryOrderByQrCode("QR-DO-001")

        advanceUntilIdle()

        assertEquals("PO-001", viewModel.scannedDeliveryOrder.value?.poNumber)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `getDeliveryOrderByQrCode error emits toast`() = runTest {
        stubDefaultUseCases()
        every { getDeliveryOrderByQrCodeUseCase(any()) } returns flowOf(Resource.Error("Missing order"))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.getDeliveryOrderByQrCode("missing")
        }

        assertEquals("Missing order", message)
        assertNull(viewModel.scannedDeliveryOrder.value)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `getLocation success updates scanned location`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.getLocation("A-01")

        advanceUntilIdle()

        assertEquals("A-01", viewModel.scannedLocation.value?.locationCode)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `getLocation error emits fallback toast`() = runTest {
        stubDefaultUseCases()
        every { getLocationByQrCodeUseCase(any()) } returns flowOf(Resource.Error(""))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.getLocation("missing")
        }

        assertEquals("Location not found", message)
        assertNull(viewModel.scannedLocation.value)
    }

    @Test
    fun `getStockByQrCode success updates scanned stock`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.getStockByQrCode("QR-STOCK-001")

        advanceUntilIdle()

        assertEquals("QR-STOCK-001", viewModel.scannedStock.value?.qrCode)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `getStockByQrCode error emits fallback toast`() = runTest {
        stubDefaultUseCases()
        every { getStockByQrCodeUseCase(any()) } returns flowOf(Resource.Error(""))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.getStockByQrCode("missing")
        }

        assertEquals("Stock not found", message)
        assertNull(viewModel.scannedStock.value)
    }

    @Test
    fun `stockIn success emits success message and refreshes stocks`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.stockIn(sampleStockInRequest())
        }

        assertEquals("Import success", message)
        verify(atLeast = 2) { getStocksUseCase() }
    }

    @Test
    fun `stockIn queued emits import queued message`() = runTest {
        stubDefaultUseCases()
        every { stockInUseCase(any()) } returns flowOf(Resource.Success(StockMutationResult.Queued))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.stockIn(sampleStockInRequest())
        }

        assertEquals("Import queued", message)
    }

    @Test
    fun `stockIn error emits failed message and stops loading`() = runTest {
        stubDefaultUseCases()
        every { stockInUseCase(any()) } returns flowOf(Resource.Error(""))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.stockIn(sampleStockInRequest())
        }

        assertEquals("Import failed", message)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `stockIn success null emits failed message and refreshes stocks`() = runTest {
        stubDefaultUseCases()
        every { stockInUseCase(any()) } returns flowOf(Resource.Success(null))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.stockIn(sampleStockInRequest())
        }

        assertEquals("Import failed", message)
        verify(atLeast = 2) { getStocksUseCase() }
    }

    @Test
    fun `updateQuantity success emits success message and refreshes stocks`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.updateQuantity(id = 10, updateQuantityRequest = sampleUpdateQuantityRequest())
        }

        assertEquals("Export success", message)
        verify(atLeast = 2) { getStocksUseCase() }
    }

    @Test
    fun `updateQuantity queued emits queued message`() = runTest {
        stubDefaultUseCases()
        every { updateQuantityUseCase(any(), any()) } returns flowOf(Resource.Success(StockMutationResult.Queued))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.updateQuantity(id = 10, updateQuantityRequest = sampleUpdateQuantityRequest())
        }

        assertEquals("Export queued", message)
    }

    @Test
    fun `updateQuantity error emits failed message and stops loading`() = runTest {
        stubDefaultUseCases()
        every { updateQuantityUseCase(any(), any()) } returns flowOf(Resource.Error(""))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.updateQuantity(id = 10, updateQuantityRequest = sampleUpdateQuantityRequest())
        }

        assertEquals("Export failed", message)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `updateQuantity success null emits failed message and refreshes stocks`() = runTest {
        stubDefaultUseCases()
        every { updateQuantityUseCase(any(), any()) } returns flowOf(Resource.Success(null))

        val viewModel = createViewModel()

        advanceUntilIdle()

        val message = awaitNextToast(viewModel, backgroundScope) {
            viewModel.updateQuantity(id = 10, updateQuantityRequest = sampleUpdateQuantityRequest())
        }

        assertEquals("Export failed", message)
        verify(atLeast = 2) { getStocksUseCase() }
    }

    @Test
    fun `clear scanned helpers reset scanned states`() = runTest {
        stubDefaultUseCases()
        val viewModel = createViewModel()

        advanceUntilIdle()

        viewModel.getDeliveryOrderByQrCode("QR-DO-001")
        viewModel.getLocation("A-01")
        viewModel.getStockByQrCode("QR-STOCK-001")

        advanceUntilIdle()

        assertTrue(viewModel.scannedDeliveryOrder.value != null)
        assertTrue(viewModel.scannedLocation.value != null)
        assertTrue(viewModel.scannedStock.value != null)

        viewModel.clearScannedProduct()
        viewModel.clearScannedStock()

        assertNull(viewModel.scannedDeliveryOrder.value)
        assertNull(viewModel.scannedLocation.value)
        assertNull(viewModel.scannedStock.value)
    }
}
