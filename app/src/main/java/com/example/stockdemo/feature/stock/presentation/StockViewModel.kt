package com.example.stockdemo.feature.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
import com.example.stockdemo.feature.stock.domain.usecase.GetDeliveryOrderByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetLocationByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStockByQrCodeUseCase
import com.example.stockdemo.feature.stock.domain.usecase.GetStocksUseCase
import com.example.stockdemo.feature.stock.domain.usecase.StockInUseCase
import com.example.stockdemo.feature.stock.domain.usecase.UpdateQuantityUseCase
import com.example.stockdemo.core.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StockViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
    private val getDeliveryOrderByQrCodeUseCase: GetDeliveryOrderByQrCodeUseCase,
    private val getLocationByQrCodeUseCase: GetLocationByQrCodeUseCase,
    private val stockInUseCase: StockInUseCase,
    private val getStockByQrCodeUseCase: GetStockByQrCodeUseCase,
    private val updateQuantityUseCase: UpdateQuantityUseCase
) : ViewModel() {

    // UI state — danh sách hàng hóa, loading, error
    private val _state = MutableStateFlow(StockUiState())
    val state: StateFlow<StockUiState> = _state.asStateFlow()

    // One-time event: toast message
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Dùng StateFlow thay vì mutableStateOf — không coupling với Compose runtime
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
                        error = result.message ?: "Lỗi không xác định",
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
                    _toastMessage.emit(result.message ?: "Không tìm thấy hàng hóa")
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
                    _toastMessage.emit(result.message ?: "Không tìm thấy thông tin tồn kho")
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
                    _toastMessage.emit(result.message ?: "Không tìm thấy vị trí")
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun stockIn(request: StockInRequest) {
        stockInUseCase(request).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _toastMessage.emit("Nhập kho thành công")
                    getStocks()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: "Nhập kho thất bại")
                }
                is Resource.Loading -> _state.update { it.copy(isLoading = result.isLoading) }
            }
        }.launchIn(viewModelScope)
    }

    fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest) {
        updateQuantityUseCase(id, updateQuantityRequest).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _toastMessage.emit("Xuất kho thành công")
                    getStocks()
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _toastMessage.emit(result.message ?: "Xuất kho thất bại")
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


