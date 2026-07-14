package com.stockdemo.warehouse.feature.stock.presentation.stocktake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockdemo.warehouse.R
import com.stockdemo.warehouse.core.common.Resource
import com.stockdemo.warehouse.core.ui.UiText
import com.stockdemo.warehouse.core.ui.asUiText
import com.stockdemo.warehouse.feature.auth.data.local.UserPreferences
import com.stockdemo.warehouse.feature.stock.domain.model.CreateStockTakeRequest
import com.stockdemo.warehouse.feature.stock.domain.model.Location
import com.stockdemo.warehouse.feature.stock.domain.model.Product
import com.stockdemo.warehouse.feature.stock.domain.model.StockTake
import com.stockdemo.warehouse.feature.stock.domain.model.StockTakeCountLine
import com.stockdemo.warehouse.feature.stock.domain.usecase.CompleteStocktakeUseCase
import com.stockdemo.warehouse.feature.stock.domain.usecase.CreateStocktakeUseCase
import com.stockdemo.warehouse.feature.stock.domain.usecase.GetLocationsUseCase
import com.stockdemo.warehouse.feature.stock.domain.usecase.GetProductsUseCase
import com.stockdemo.warehouse.feature.stock.domain.usecase.SyncMasterProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StocktakeUiState(
    val locations: List<Location> = emptyList(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val review: StockTake? = null
)

sealed interface StocktakeEvent {
    data object Reconciled : StocktakeEvent
    data class Error(val message: UiText) : StocktakeEvent
}

@HiltViewModel
class StocktakeViewModel @Inject constructor(
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val createStocktakeUseCase: CreateStocktakeUseCase,
    private val completeStocktakeUseCase: CompleteStocktakeUseCase,
    private val syncMasterProductsUseCase: SyncMasterProductsUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StocktakeUiState())
    val uiState: StateFlow<StocktakeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StocktakeEvent>()
    val events: SharedFlow<StocktakeEvent> = _events.asSharedFlow()

    init {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            syncMasterProductsUseCase().collect { }
            _uiState.update {
                it.copy(
                    locations = getLocationsUseCase(),
                    products = getProductsUseCase(),
                    isLoading = false
                )
            }
        }
    }

    fun submitCount(locationId: Int, note: String?, items: List<StockTakeCountLine>) {
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: 0
            val request = CreateStockTakeRequest(
                locationId = locationId,
                note = note,
                createdBy = userId,
                items = items
            )
            createStocktakeUseCase(request).collect { result ->
                when (result) {
                    is Resource.Loading ->
                        _uiState.update { it.copy(isSubmitting = result.isLoading) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isSubmitting = false, review = result.data)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSubmitting = false) }
                        _events.emit(StocktakeEvent.Error(result.message.asUiText(R.string.stocktake_failed)))
                    }
                }
            }
        }
    }

    fun confirmReconcile() {
        val session = _uiState.value.review ?: return
        viewModelScope.launch {
            completeStocktakeUseCase(session.stockTakeId).collect { result ->
                when (result) {
                    is Resource.Loading ->
                        _uiState.update { it.copy(isSubmitting = result.isLoading) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSubmitting = false, review = null) }
                        _events.emit(StocktakeEvent.Reconciled)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSubmitting = false) }
                        _events.emit(StocktakeEvent.Error(result.message.asUiText(R.string.stocktake_failed)))
                    }
                }
            }
        }
    }

    fun dismissReview() {
        _uiState.update { it.copy(review = null) }
    }
}
