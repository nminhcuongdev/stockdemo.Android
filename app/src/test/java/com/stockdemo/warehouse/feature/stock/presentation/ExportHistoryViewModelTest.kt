package com.stockdemo.warehouse.feature.stock.presentation

import androidx.paging.PagingData
import com.stockdemo.warehouse.feature.stock.domain.model.StockOut
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import com.stockdemo.warehouse.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class ExportHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: StockRepository = mockk()

    @Test
    fun `constructor requests stock out history page 10`() {
        every { repository.getStockOutHistory(10) } returns flowOf(PagingData.empty())

        val viewModel = ExportHistoryViewModel(repository)

        assertNotNull(viewModel.pagingData)
        verify(exactly = 1) { repository.getStockOutHistory(10) }
    }
}
