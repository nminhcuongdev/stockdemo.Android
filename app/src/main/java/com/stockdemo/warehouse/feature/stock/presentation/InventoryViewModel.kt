package com.stockdemo.warehouse.feature.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockdemo.warehouse.core.common.Resource
import com.stockdemo.warehouse.feature.stock.data.remote.EpcMappingDto
import com.stockdemo.warehouse.feature.stock.data.remote.StockRemoteDataSource
import com.stockdemo.warehouse.feature.stock.domain.model.RFIDTag
import com.stockdemo.warehouse.feature.stock.domain.model.Stock
import com.stockdemo.warehouse.feature.stock.domain.repository.StockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Owns the RFID scan state (scanned tags, scanning flag, last error) so it survives
 *  configuration changes such as rotation, and resolves scanned EPCs to product info using the
 *  server-side EPC <-> Stock mapping (GET/POST/DELETE /EpcMappings) shared across all devices.
 *  Stock details for the assign picker and the resolved display come from the same offline-first
 *  stock cache the rest of the app uses. Hardware control (RfidManager) stays in the screen since
 *  it is Context/lifecycle-bound; the screen just forwards tag reads and scan-state changes here. */
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

    private val _tags = MutableStateFlow<List<RFIDTag>>(emptyList())
    val tags: StateFlow<List<RFIDTag>> = _tags.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadStocks()
        loadMappings()
    }

    /** Called for each hardware tag read. Only mapped EPCs are kept, and each EPC is added at
     *  most once for the whole session (until [clearTags]); unmapped or duplicate reads are
     *  ignored so they are neither shown nor counted. */
    fun onTagScanned(epc: String, rssi: Int) {
        if (!epcToStock.value.containsKey(epc)) return
        if (_tags.value.any { it.epc == epc }) return
        _tags.value = _tags.value + RFIDTag(epc, rssi, 1, System.currentTimeMillis())
    }

    fun setScanning(scanning: Boolean) {
        _isScanning.value = scanning
    }

    fun setError(message: String?) {
        _errorMessage.value = message
    }

    fun clearTags() {
        _tags.value = emptyList()
        _errorMessage.value = null
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
                    else _errorMessage.value = response.message
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun refreshStocks() {
        loadStocks()
        loadMappings()
    }

    fun assignEpc(epc: String, qrCode: String) {
        viewModelScope.launch {
            runCatching { remoteDataSource.assignEpc(epc, qrCode) }
                .onSuccess {
                    if (it.success) loadMappings() else _errorMessage.value = it.message
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }

    fun unassignEpc(epc: String) {
        viewModelScope.launch {
            runCatching { remoteDataSource.deleteEpcMapping(epc) }
                .onSuccess {
                    if (it.success) loadMappings() else _errorMessage.value = it.message
                }
                .onFailure { _errorMessage.value = it.message }
        }
    }
}
