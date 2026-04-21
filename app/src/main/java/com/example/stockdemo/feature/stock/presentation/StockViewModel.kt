package com.example.stockdemo.feature.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.core.ui.UiText
import com.example.stockdemo.core.ui.asUiText
import com.example.stockdemo.feature.stock.domain.model.DeliveryOrder
import com.example.stockdemo.feature.stock.domain.model.Location
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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
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
    private val syncMasterProductsUseCase: SyncMasterProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StockUiState())
    val state: StateFlow<StockUiState> = _state.asStateFlow()

    private val _toastMessage = MutableSharedFlow<UiText>()
    val toastMessage: SharedFlow<UiText> = _toastMessage.asSharedFlow()

    private val _scannedDeliveryOrder = MutableStateFlow<DeliveryOrder?>(null)
    val scannedDeliveryOrder: StateFlow<DeliveryOrder?> = _scannedDeliveryOrder.asStateFlow()

    private val _scannedLocation = MutableStateFlow<Location?>(null)
    val scannedLocation: StateFlow<Location?> = _scannedLocation.asStateFlow()

    private val _scannedStock = MutableStateFlow<Stock?>(null)
    val scannedStock: StateFlow<Stock?> = _scannedStock.asStateFlow()

    init {
        syncMasterProducts()
        getStocks()
    }

    fun syncMasterProducts() {
        collectResource(
            syncMasterProductsUseCase(),
            onError = { message ->
                _toastMessage.emit(message.asUiText(R.string.product_master_sync_failed))
            }
        )
    }

    fun getStocks() {
        collectResource(
            getStocksUseCase(),
            onSuccess = { stocks ->
                _state.update {
                    it.copy(
                        stocks = stocks ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
            },
            onError = { message ->
                _state.update {
                    it.copy(
                        error = message.asUiText(R.string.unknown_error),
                        isLoading = false
                    )
                }
            },
            onLoading = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            }
        )
    }

    fun getDeliveryOrderByQrCode(qrCode: String) {
        collectResource(
            getDeliveryOrderByQrCodeUseCase(qrCode),
            onSuccess = { deliveryOrder ->
                _scannedDeliveryOrder.value = deliveryOrder
                _state.update { it.copy(isLoading = false) }
            },
            onError = { message ->
                _state.update { it.copy(isLoading = false) }
                _toastMessage.emit(message.asUiText(R.string.toast_delivery_order_not_found))
            },
            onLoading = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            }
        )
    }

    fun getStockByQrCode(qrCode: String) {
        collectResource(
            getStockByQrCodeUseCase(qrCode),
            onSuccess = { stock ->
                _scannedStock.value = stock
                _state.update { it.copy(isLoading = false) }
            },
            onError = { message ->
                _state.update { it.copy(isLoading = false) }
                _toastMessage.emit(message.asUiText(R.string.toast_stock_not_found))
            },
            onLoading = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            }
        )
    }

    fun getLocation(code: String) {
        collectResource(
            getLocationByQrCodeUseCase(code),
            onSuccess = { location ->
                _scannedLocation.value = location
                _state.update { it.copy(isLoading = false) }
            },
            onError = { message ->
                _state.update { it.copy(isLoading = false) }
                _toastMessage.emit(message.asUiText(R.string.toast_location_not_found))
            },
            onLoading = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            }
        )
    }

    fun stockIn(request: StockInRequest) {
        collectResource(
            stockInUseCase(request),
            onSuccess = { mutationResult ->
                val messageRes = when (mutationResult) {
                    is StockMutationResult.Synced -> R.string.toast_import_success
                    StockMutationResult.Queued -> R.string.toast_import_queued
                    null -> R.string.toast_import_failed
                }
                _toastMessage.emit(UiText.StringResource(messageRes))
                getStocks()
            },
            onError = { message ->
                _state.update { it.copy(isLoading = false) }
                _toastMessage.emit(message.asUiText(R.string.toast_import_failed))
            },
            onLoading = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            }
        )
    }

    fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest) {
        collectResource(
            updateQuantityUseCase(id, updateQuantityRequest),
            onSuccess = { mutationResult ->
                val messageRes = when (mutationResult) {
                    is StockMutationResult.Synced -> R.string.toast_export_success
                    StockMutationResult.Queued -> R.string.toast_export_queued
                    null -> R.string.toast_export_failed
                }
                _toastMessage.emit(UiText.StringResource(messageRes))
                getStocks()
            },
            onError = { message ->
                _state.update { it.copy(isLoading = false) }
                _toastMessage.emit(message.asUiText(R.string.toast_export_failed))
            },
            onLoading = { isLoading ->
                _state.update { it.copy(isLoading = isLoading) }
            }
        )
    }

    fun clearScannedProduct() {
        _scannedDeliveryOrder.value = null
        _scannedLocation.value = null
    }

    fun clearScannedStock() {
        _scannedStock.value = null
    }

    private fun <T> collectResource(
        flow: Flow<Resource<T>>,
        onSuccess: suspend (T?) -> Unit = {},
        onError: suspend (String?) -> Unit = {},
        onLoading: suspend (Boolean) -> Unit = {}
    ) {
        flow.onEach { result ->
            when (result) {
                is Resource.Success -> onSuccess(result.data)
                is Resource.Error -> onError(result.message)
                is Resource.Loading -> onLoading(result.isLoading)
            }
        }.launchIn(viewModelScope)
    }
}
