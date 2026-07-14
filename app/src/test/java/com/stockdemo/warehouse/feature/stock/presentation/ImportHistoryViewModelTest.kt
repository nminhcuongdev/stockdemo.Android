package com.stockdemo.warehouse.feature.stock.presentation

import androidx.paging.PagingData
import com.stockdemo.warehouse.feature.stock.domain.model.StockIn
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import com.stockdemo.warehouse.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertNotNull
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class ImportHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: StockRepository = mockk()

    @Test
    fun `constructor requests stock in history page 10`() {
        every { repository.getStockInHistory(10) } returns flowOf(PagingData.empty())

        val viewModel = ImportHistoryViewModel(repository)

        assertNotNull(viewModel.pagingData)
        verify(exactly = 1) { repository.getStockInHistory(10) }
    }
}
