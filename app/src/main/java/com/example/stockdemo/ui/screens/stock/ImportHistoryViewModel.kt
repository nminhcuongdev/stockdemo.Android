package com.example.stockdemo.ui.screens.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.stockdemo.domain.model.stock.StockIn
import com.example.stockdemo.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ImportHistoryViewModel @Inject constructor(
    private val repository: StockRepository
) : ViewModel() {

    val pagingData: Flow<PagingData<StockIn>> = repository
        .getStockInHistory(pageSize = 10)
        .cachedIn(viewModelScope)
}
