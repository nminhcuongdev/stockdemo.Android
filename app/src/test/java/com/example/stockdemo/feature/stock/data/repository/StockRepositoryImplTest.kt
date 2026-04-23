package com.example.stockdemo.feature.stock.data.repository

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.data.local.StockLocalDataSource
import com.example.stockdemo.feature.stock.data.remote.StockRemoteDataSource
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Product
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.StockMutationResult
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.sync.StockSyncCoordinator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockRepositoryImplTest {

    private val localDataSource: StockLocalDataSource = mockk()
    private val remoteDataSource: StockRemoteDataSource = mockk()
    private val syncCoordinator: StockSyncCoordinator = mockk(relaxed = true)

    private fun createRepository(): StockRepositoryImpl {
        return StockRepositoryImpl(localDataSource, remoteDataSource, syncCoordinator)
    }

    private fun sampleStock(): Stock {
        return Stock(
            stockId = 1,
            productId = 11,
            locationId = 21,
            quantity = 8,
            qrCode = "QR-001",
            lastUpdated = "2026-04-23T00:00:00",
            product = Product(
                productId = 11,
                productCode = "P-001",
                productName = "Carton",
                description = "Carton box",
                unit = "pcs",
                isActive = true,
                createdDate = "2026-04-23T00:00:00"
            ),
            location = Location(
                locationId = 21,
                locationCode = "A-01",
                locationName = "Shelf A-01",
                isActive = true,
                createdDate = "2026-04-23T00:00:00"
            )
        )
    }

    private fun sampleStockInRequest(): StockInRequest {
        return StockInRequest(
            locationId = 21,
            productId = 11,
            qrCode = "QR-001",
            quantity = 5,
            userId = 99
        )
    }

    @Test
    fun `getAllStocks offline with cache emits cached data and skips remote`() = runTest {
        coEvery { localDataSource.getCachedStocks() } returns listOf(sampleStock())
        every { syncCoordinator.isNetworkAvailable() } returns false

        val results = createRepository().getAllStocks().toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val success = results[1] as Resource.Success
        assertEquals(1, success.data?.size)
        assertEquals("QR-001", success.data?.first()?.qrCode)
        coVerify(exactly = 0) { remoteDataSource.getAllStocks() }
    }

    @Test
    fun `getAllStocks offline without cache emits offline error`() = runTest {
        coEvery { localDataSource.getCachedStocks() } returns emptyList()
        every { syncCoordinator.isNetworkAvailable() } returns false

        val results = createRepository().getAllStocks().toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Error)
        val error = results[1] as Resource.Error
        assertEquals("No cached stock data available offline", error.message)
    }

    @Test
    fun `getProductByQrCode expands candidate codes before local lookup`() = runTest {
        coEvery { localDataSource.getProductByCodes(any()) } returns sampleStock().product

        val results = createRepository().getProductByQrCode(" QR-001 ; ALT-001 ")
            .toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Success)
        coVerify(exactly = 1) {
            localDataSource.getProductByCodes(listOf("QR-001 ; ALT-001", "QR-001", "ALT-001"))
        }
    }

    @Test
    fun `stockIn without network queues pending work`() = runTest {
        every { syncCoordinator.isNetworkAvailable() } returns false

        val results = createRepository().stockIn(sampleStockInRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Success)
        val success = results[1] as Resource.Success
        assertEquals(StockMutationResult.Queued, success.data)
        coVerify(exactly = 1) { syncCoordinator.queueStockIn(sampleStockInRequest()) }
    }

    @Test
    fun `updateQuantity remote failure falls back to queue`() = runTest {
        val request = UpdateQuantityRequest(quantity = 3, createdBy = 99)
        every { syncCoordinator.isNetworkAvailable() } returns true
        coEvery { remoteDataSource.updateQuantity(10, request) } throws IllegalStateException("boom")

        val results = createRepository().updateQuantity(10, request).toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Success)
        val success = results[1] as Resource.Success
        assertEquals(StockMutationResult.Queued, success.data)
        coVerify(exactly = 1) { syncCoordinator.queueStockOut(10, request) }
    }
}
