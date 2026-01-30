package com.example.satostockmanagement.viewmodel.stock

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.satostockmanagement.models.Location
import com.example.satostockmanagement.models.deliveryOrders.DeliveryOrder
import com.example.satostockmanagement.models.stocks.Stock
import com.example.satostockmanagement.models.stocks.StockInRequest
import com.example.satostockmanagement.models.stocks.UpdateQuantityRequest
import com.example.satostockmanagement.repository.StockRepository
import com.example.satostockmanagement.repository.UserRepository
import com.example.satostockmanagement.screens.LoginUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.internal.http2.ErrorCode

class StockViewModel(private val repository: StockRepository): ViewModel() {

    var stocks by mutableStateOf<List<Stock>>(emptyList())
        private set

    var scannedProduct by mutableStateOf<DeliveryOrder?>(null)
        private set

    var scannedStock by mutableStateOf<Stock?>(null)
        private set

    var scannedLocation by mutableStateOf<Location?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    fun getLocation(qrCode: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getLocationByQrCode(qrCode)
                if (response.success) {
                    scannedLocation = response.data ?: null
                } else {
                    // Nếu success = false, lấy thông báo lỗi từ API
                    errorMessage = response.message ?: "Lỗi không xác định"
                }
            } catch (e: Exception) {
                // Xử lý lỗi kết nối mạng, server die, v.v.
                errorMessage = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateQuantity(id: Int, updateQuantityRequest: UpdateQuantityRequest) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.updateQuantity(id, updateQuantityRequest)
                if (response.success) {
                    _toastMessage.emit("Cập nhật số lượng thành công!")
                    getStocks() // Làm mới danh sách
                } else {
                    errorMessage = response.message ?: "Lỗi cập nhật số lượng"
                }
            } catch (e: Exception) {
                _toastMessage.emit("Lỗi kết nối: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun stockIn(stockInRequest: StockInRequest) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.stockIn(stockInRequest)
                if (response.success) {
                    _toastMessage.emit("Nhập kho thành công!") // Phát sự kiện thành công
                    getStocks() // Làm mới danh sách
                } else {
                    errorMessage = response.message ?: "Lỗi nhập kho"
                }
            } catch (e: Exception) {
                _toastMessage.emit("Lỗi kết nối: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun getStocks() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getAllStocks()

                if (response.success) {
                    // Nếu success = true, gán data (danh sách stock) vào biến stocks
                    stocks = response.data ?: emptyList()
                } else {
                    // Nếu success = false, lấy thông báo lỗi từ API
                    errorMessage = response.message ?: "Lỗi không xác định"
                }
            } catch (e: Exception) {
                // Xử lý lỗi kết nối mạng, server die, v.v.
                errorMessage = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun getDObyQrcode(qrcode: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getDObyQrCode(qrcode)
                if (response.success) {
                    // Nếu success = true, gán data (danh sách stock) vào biến stocks
                    scannedProduct = response.data ?: null
                } else {
                    // Nếu success = false, lấy thông báo lỗi từ API
                    errorMessage = response.message ?: "Lỗi không xác định"
                }
            } catch (e: Exception) {
                // Xử lý lỗi kết nối mạng, server die, v.v.
                errorMessage = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun getStockByQrCode(qrcode: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getStockByQrCode(qrcode)
                if (response.success) {
                    scannedStock = response.data ?: null
                } else {
                    errorMessage = response.message ?: "Lỗi không xác định"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearScannedProduct() {
        scannedProduct = null
    }

    fun clearScannedStock() {
        scannedStock = null
    }
}