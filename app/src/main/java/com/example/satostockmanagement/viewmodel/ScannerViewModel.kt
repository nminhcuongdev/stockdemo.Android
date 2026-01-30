package com.example.satostockmanagement.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ScannerViewModel : ViewModel() {
    val scanHistory = mutableStateListOf<String>()

    fun addScanResult(code: String?) {
        code?.let {
            if (it.isNotEmpty()) {
                scanHistory.add(0, it) // Thêm lên đầu danh sách
            }
        }
    }
}