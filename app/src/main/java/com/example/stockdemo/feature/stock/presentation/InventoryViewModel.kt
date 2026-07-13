package com.example.stockdemo.feature.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.data.remote.EpcMappingDto
import com.example.stockdemo.feature.stock.data.remote.StockRemoteDataSource
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Resolves scanned RFID EPCs to product info using the server-side EPC <-> Stock mapping
 *  (GET/POST/DELETE /EpcMappings), so pairings are shared across all devices instead of living
 *  only in each device's local Room cache. Stock details for both the assign picker and the
 *  resolved display come from the same offline-first stock cache the rest of the app uses. */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val remoteDataSource: StockRemoteDataSource,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _stocks = MutableStateFlow<List<Stock>>(emptyList())
    val stocks: StateFlow<List<Stock>> = _stocks.asStateFlow()

    private val _mappings = MutableStateFlow<List<EpcMappingDto>>(emptyList())

    /** epc -> Stock, rebuilt whenever the server mappings or the stock list change. */
    val epcToStock: StateFlow<Map<String, Stock>> =
        combine(_mappings, _stocks) { mappings, stocks ->
            val byQrCode = stocks.associateBy { it.qrCode }
            mappings.mapNotNull { mapping -> byQrCode[mapping.qrCode]?.let { mapping.epc to it } }.toMap()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    init {
        loadStocks()
        loadMappings()
    }

    private fun loadStocks() {
        viewModelScope.launch {
            stockRepository.getAllStocks().collect { resource ->
                if (resource is Resource.Success) {
                    _stocks.value = resource.data ?: emptyList()
                }
            }
        }
    }

    private fun loadMappings() {
        viewModelScope.launch {
            runCatching { remoteDataSource.getEpcMappings() }
                .onSuccess { response ->
                    if (response.success) _mappings.value = response.data ?: emptyList()
                }
        }
    }

    fun refreshStocks() {
        loadStocks()
        loadMappings()
    }

    fun assignEpc(epc: String, qrCode: String) {
        viewModelScope.launch {
            runCatching { remoteDataSource.assignEpc(epc, qrCode) }
                .onSuccess { if (it.success) loadMappings() }
        }
    }

    fun unassignEpc(epc: String) {
        viewModelScope.launch {
            runCatching { remoteDataSource.deleteEpcMapping(epc) }
                .onSuccess { if (it.success) loadMappings() }
        }
    }
}
