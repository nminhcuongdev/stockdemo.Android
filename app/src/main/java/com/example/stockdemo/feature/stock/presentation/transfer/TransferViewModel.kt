package com.example.stockdemo.feature.stock.presentation.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.core.ui.UiText
import com.example.stockdemo.core.ui.asUiText
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.TransferStockRequest
import com.example.stockdemo.feature.stock.domain.usecase.GetLocationsUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStocksUseCase
import com.example.stockdemo.feature.stock.domain.usecase.SyncMasterProductsUseCase
import com.example.stockdemo.feature.stock.domain.usecase.TransferStockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransferUiState(
    val stocks: List<Stock> = emptyList(),
    val locations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false
)

sealed interface TransferEvent {
    data object Success : TransferEvent
    data class Error(val message: UiText) : TransferEvent
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
    private val getLocationsUseCase: GetLocationsUseCase,
    private val transferStockUseCase: TransferStockUseCase,
    private val syncMasterProductsUseCase: SyncMasterProductsUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TransferEvent>()
    val events: SharedFlow<TransferEvent> = _events.asSharedFlow()

    private var stocksJob: Job? = null

    init {
        loadStocks()
        viewModelScope.launch {
            // Ensure master data (locations) is present, then load it from cache.
            syncMasterProductsUseCase().collect { }
            _uiState.update { it.copy(locations = getLocationsUseCase()) }
        }
    }

    private fun loadStocks() {
        stocksJob?.cancel()
        stocksJob = getStocksUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> _uiState.update {
                    it.copy(stocks = result.data ?: emptyList(), isLoading = false)
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false) }
                is Resource.Loading -> _uiState.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun transfer(sourceStockId: Int, toLocationId: Int, quantity: Int) {
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: 0
            val request = TransferStockRequest(
                sourceStockId = sourceStockId,
                toLocationId = toLocationId,
                quantity = quantity,
                createdBy = userId
            )
            transferStockUseCase(request).collect { result ->
                when (result) {
                    is Resource.Loading ->
                        _uiState.update { it.copy(isSubmitting = result.isLoading) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSubmitting = false) }
                        _events.emit(TransferEvent.Success)
                        loadStocks()
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSubmitting = false) }
                        _events.emit(TransferEvent.Error(result.message.asUiText(R.string.transfer_failed)))
                    }
                }
            }
        }
    }
}
