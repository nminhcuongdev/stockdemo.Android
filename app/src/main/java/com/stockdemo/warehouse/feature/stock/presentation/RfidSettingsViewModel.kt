package com.stockdemo.warehouse.feature.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockdemo.warehouse.core.rfid.RfidPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shared by the Inventory screen (applies settings + inline power control) and the
 *  Settings screen (edits the defaults). Backed by [RfidPreferences] so choices persist. */
@HiltViewModel
class RfidSettingsViewModel @Inject constructor(
    private val prefs: RfidPreferences
) : ViewModel() {

    val txPower: StateFlow<Int> = prefs.txPower
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RfidPreferences.DEFAULT_TX_POWER)

    val session: StateFlow<String> = prefs.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RfidPreferences.DEFAULT_SESSION)

    val qDynamic: StateFlow<Boolean> = prefs.qDynamic
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RfidPreferences.DEFAULT_Q_DYNAMIC)

    val workMode: StateFlow<String> = prefs.workMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RfidPreferences.DEFAULT_WORK_MODE)

    val filterDuplicate: StateFlow<Boolean> = prefs.filterDuplicate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RfidPreferences.DEFAULT_FILTER_DUPLICATE)

    fun setTxPower(value: Int) = viewModelScope.launch { prefs.setTxPower(value) }
    fun setSession(value: String) = viewModelScope.launch { prefs.setSession(value) }
    fun setQDynamic(value: Boolean) = viewModelScope.launch { prefs.setQDynamic(value) }
    fun setWorkMode(value: String) = viewModelScope.launch { prefs.setWorkMode(value) }
    fun setFilterDuplicate(value: Boolean) = viewModelScope.launch { prefs.setFilterDuplicate(value) }
}
