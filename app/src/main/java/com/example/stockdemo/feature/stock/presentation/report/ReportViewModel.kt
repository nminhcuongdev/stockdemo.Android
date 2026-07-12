package com.example.stockdemo.feature.stock.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.stock.domain.model.StockMovementReport
import com.example.stockdemo.feature.stock.domain.usecase.GetStockMovementReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class ReportRange(val days: Int) { WEEK(7), MONTH(30), QUARTER(90), ALL(-1) }

data class ReportUiState(
    val report: StockMovementReport? = null,
    val isLoading: Boolean = false,
    val range: ReportRange = ReportRange.MONTH,
    val fromLabel: String = "",
    val toLabel: String = ""
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getStockMovementReportUseCase: GetStockMovementReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(ReportRange.MONTH)
    }

    fun selectRange(range: ReportRange) {
        if (range != _uiState.value.range || _uiState.value.report == null) {
            load(range)
        }
    }

    private fun load(range: ReportRange) {
        val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val labelFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        val toCal = Calendar.getInstance()
        val fromCal = Calendar.getInstance()
        if (range == ReportRange.ALL) {
            fromCal.set(2000, Calendar.JANUARY, 1)
        } else {
            fromCal.add(Calendar.DAY_OF_YEAR, -range.days)
        }

        val fromApi = apiFmt.format(fromCal.time)
        val toApi = apiFmt.format(toCal.time)

        _uiState.update {
            it.copy(
                range = range,
                fromLabel = labelFmt.format(fromCal.time),
                toLabel = labelFmt.format(toCal.time),
                isLoading = true
            )
        }

        loadJob?.cancel()
        loadJob = getStockMovementReportUseCase(fromApi, toApi).onEach { result ->
            when (result) {
                is Resource.Loading -> _uiState.update { it.copy(isLoading = result.isLoading) }
                is Resource.Success -> _uiState.update { it.copy(report = result.data, isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false) }
            }
        }.launchIn(viewModelScope)
    }
}
