package com.stockdemo.warehouse.feature.stock.sync

import android.content.Context
import android.util.Log
import com.stockdemo.warehouse.core.network.model.BaseResponse
import com.stockdemo.warehouse.core.ui.util.NetworkManager
import com.stockdemo.warehouse.feature.stock.data.local.PendingStockInEntity
import com.stockdemo.warehouse.feature.stock.data.local.PendingStockOutEntity
import com.stockdemo.warehouse.feature.stock.data.local.StockLocalDataSource
import com.stockdemo.warehouse.feature.stock.data.remote.StockDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockRemoteDataSource
import com.stockdemo.warehouse.feature.stock.domain.model.StockInRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockSyncCoordinatorTest {

    private val context: Context = mockk(relaxed = true)
    private val localDataSource: StockLocalDataSource = mockk(relaxed = true)
    private val remoteDataSource: StockRemoteDataSource = mockk()

    @Before
    fun setUp() {
        mockkStaticLog()
        mockkObject(NetworkManager)
        mockkObject(StockSyncScheduler)
        every { StockSyncScheduler.schedule(any()) } returns Unit
        every { NetworkManager.isNetworkAvailable(any()) } returns true
    }

    @After
    fun tearDown() = unmockkAll()

    private fun mockkStaticLog() {
        io.mockk.mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.d(any(), any<String>(), any()) } returns 0
    }

    private fun coordinator() = StockSyncCoordinator(context, localDataSource, remoteDataSource)

    private fun stockInRequest() = StockInRequest(
        locationId = 21, productId = 11, qrCode = "QR-001", quantity = 5, userId = 99
    )

    private fun pendingStockIn(id: Long) = PendingStockInEntity(
        pendingId = id, productId = 11, locationId = 21, qrCode = "QR-001",
        quantity = 5, userId = 99, createdAt = 0L
    )

    private fun pendingStockOut(id: Long) = PendingStockOutEntity(
        pendingId = id, stockId = 7, quantity = 3, createdBy = 99, createdAt = 0L
    )

    private fun okResponse() = BaseResponse<StockDto>(success = true, message = "ok", data = null)
    private fun failResponse(msg: String) = BaseResponse<StockDto>(success = false, message = msg, data = null)

    @Test
    fun `queueStockIn persists locally and schedules sync`() = runTest {
        coordinator().queueStockIn(stockInRequest())

        coVerify(exactly = 1) { localDataSource.insertPendingStockIn(any()) }
        verify(exactly = 1) { StockSyncScheduler.schedule(context) }
    }

    @Test
    fun `queueStockOut persists locally and schedules sync`() = runTest {
        coordinator().queueStockOut(stockId = 7, request = mockk(relaxed = true))

        coVerify(exactly = 1) { localDataSource.insertPendingStockOut(any()) }
        verify(exactly = 1) { StockSyncScheduler.schedule(context) }
    }

    @Test
    fun `syncPendingStockIns deletes each item on success`() = runTest {
        coEvery { localDataSource.getPendingStockIns() } returnsMany
            listOf(listOf(pendingStockIn(1)), emptyList())
        coEvery { remoteDataSource.stockIn(any()) } returns okResponse()

        coordinator().syncPendingStockIns()

        coVerify(exactly = 1) { localDataSource.deletePendingStockIn(1) }
        coVerify(exactly = 0) { localDataSource.markPendingStockInFailed(any(), any()) }
    }

    @Test
    fun `syncPendingStockIns marks failed when server rejects`() = runTest {
        coEvery { localDataSource.getPendingStockIns() } returnsMany
            listOf(listOf(pendingStockIn(1)), emptyList())
        coEvery { remoteDataSource.stockIn(any()) } returns failResponse("duplicate")

        coordinator().syncPendingStockIns()

        coVerify(exactly = 1) { localDataSource.markPendingStockInFailed(1, "duplicate") }
        coVerify(exactly = 0) { localDataSource.deletePendingStockIn(any()) }
    }

    @Test
    fun `syncPendingStockIns marks failed and rethrows on exception`() = runTest {
        coEvery { localDataSource.getPendingStockIns() } returns listOf(pendingStockIn(1))
        coEvery { remoteDataSource.stockIn(any()) } throws IllegalStateException("network down")

        var thrown: Throwable? = null
        try {
            coordinator().syncPendingStockIns()
        } catch (e: IllegalStateException) {
            thrown = e
        }

        assertNotNull(thrown)
        assertEquals("network down", thrown?.message)
        coVerify(exactly = 1) { localDataSource.markPendingStockInFailed(1, "network down") }
    }

    @Test
    fun `syncPendingStockOuts deletes each item on success`() = runTest {
        coEvery { localDataSource.getPendingStockOuts() } returnsMany
            listOf(listOf(pendingStockOut(2)), emptyList())
        coEvery { remoteDataSource.updateQuantity(any(), any()) } returns okResponse()

        coordinator().syncPendingStockOuts()

        coVerify(exactly = 1) { localDataSource.deletePendingStockOut(2) }
    }

    @Test
    fun `syncPendingStockOuts marks failed when server rejects`() = runTest {
        coEvery { localDataSource.getPendingStockOuts() } returnsMany
            listOf(listOf(pendingStockOut(2)), emptyList())
        coEvery { remoteDataSource.updateQuantity(any(), any()) } returns failResponse("gone")

        coordinator().syncPendingStockOuts()

        coVerify(exactly = 1) { localDataSource.markPendingStockOutFailed(2, "gone") }
    }

    @Test
    fun `syncPendingStockIns with no pending items does nothing remote`() = runTest {
        coEvery { localDataSource.getPendingStockIns() } returns emptyList()

        coordinator().syncPendingStockIns()

        coVerify(exactly = 0) { remoteDataSource.stockIn(any()) }
    }
}
