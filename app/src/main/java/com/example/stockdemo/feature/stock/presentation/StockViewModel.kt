package com.example.stockdemo.feature.stock.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.usecase.GetDeliveryOrderByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetLocationByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStockByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStocksUseCase
import com.example.stockdemo.feature.stock.domain.usecase.StockInUseCase
import com.example.stockdemo.feature.stock.domain.usecase.UpdateQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@HiltViewModel
class StockViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
    private val getDeliveryOrderByQrCodeUseCase: GetDeliveryOrderByQrCodeUseCase,
    private val getLocationByQrCodeUseCase: GetLocationByQrCodeUseCase,
    private val stockInUseCase: StockInUseCase,
    private val getStockByQrCodeUseCase: GetStockByQrCodeUseCase,
    private val updateQuantityUseCase: UpdateQuantityUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(StockUiState())
    val state: StateFlow<StockUiState> = _state.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _scannedProduct = MutableStateFlow<DeliveryOrder?>(null)
    val scannedProduct: StateFlow<DeliveryOrder?> = _scannedProduct.asStateFlow()

    private val _scannedLocation = MutableStateFlow<Location?>(null)
    val scannedLocation: StateFlow<Location?> = _scannedLocation.asStateFlow()

    private val _scannedStock = MutableStateFlow<Stock?>(null)
    val scannedStock: StateFlow<Stock?> = _scannedStock.asStateFlow()

    init {
        getStocks()
    }

    fun getStocks() {
        getStocksUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> _state.update {
                    it.copy(
                        stocks = result.data ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(
                        error = result.message ?: context.getString(R.string.unknown_error),
                        isLoading = false
                    )
                }
                is Resource.Loading -> _state.update {
                    it.copy(isLoading = result.isLoading)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getDeliveryOrderByQrCode(qrCode: String) {
        getDeliveryOrderByQrCodeUseCase(qrCode).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _scannedProduct.value = result.data
                    _state.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: context.getString(R.string.toast_product_not_found))
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun getStockByQrCode(qrCode: String) {
        getStockByQrCodeUseCase(qrCode).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _scannedStock.value = result.data
                    _state.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: context.getString(R.string.toast_stock_not_found))
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun getLocation(code: String) {
        getLocationByQrCodeUseCase(code).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _scannedLocation.value = result.data
                    _state.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: context.getString(R.string.toast_location_not_found))
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun stockIn(request: StockInRequest) {
        stockInUseCase(request).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _toastMessage.emit(context.getString(R.string.toast_import_success))
                    getStocks()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: context.getString(R.string.toast_import_failed))
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest) {
        updateQuantityUseCase(id, updateQuantityRequest).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _toastMessage.emit(context.getString(R.string.toast_export_success))
                    getStocks()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: context.getString(R.string.toast_export_failed))
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun clearScannedProduct() {
        _scannedProduct.value = null
        _scannedLocation.value = null
    }

    fun clearScannedStock() {
        _scannedStock.value = null
    }
}
