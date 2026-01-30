package com.example.satostockmanagement.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.*import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.cipherlab.rfid.ClResult
import com.cipherlab.rfid.GeneralString
import com.cipherlab.rfid.InventoryType
import com.cipherlab.rfidapi.RfidManager
import com.example.satostockmanagement.ui.theme.PrimaryColor
import java.text.SimpleDateFormat
import java.util.*

// Data class để lưu thông tin thẻ RFID
data class RFIDTag(
    val epc: String,
    val rssi: Int,
    val count: Int,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Quản lý RfidManager
    val rfidManager = remember { RfidManager.InitInstance(context) }

    // State quản lý danh sách thẻ và trạng thái quét
    var tagList by remember { mutableStateOf<List<RFIDTag>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalReads by remember { mutableStateOf(0) }

    // Đăng ký BroadcastReceiver để nhận dữ liệu RFID
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == GeneralString.Intent_RFIDSERVICE_TAG_DATA) {
                    val response = intent.getIntExtra(GeneralString.EXTRA_RESPONSE, -1)

                    if (response == GeneralString.RESPONSE_OPERATION_SUCCESS) {
                        val epc = intent.getStringExtra(GeneralString.EXTRA_EPC) ?: ""
                        val rssi = intent.getIntExtra(GeneralString.EXTRA_DATA_RSSI, 0)

                        tagList = tagList.toMutableList().apply {
                            val existingIndex = indexOfFirst { it.epc == epc }
                            if (existingIndex != -1) {
                                this[existingIndex] = this[existingIndex].copy(
                                    count = this[existingIndex].count + 1,
                                    rssi = rssi,
                                    timestamp = System.currentTimeMillis()
                                )
                            } else {
                                add(RFIDTag(epc, rssi, 1, System.currentTimeMillis()))
                            }
                        }
                        totalReads++
                    }
                }
            }
        }

        val filter = IntentFilter(GeneralString.Intent_RFIDSERVICE_TAG_DATA)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
            rfidManager?.Release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kiểm kê RFID") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Hàng nút điều khiển
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val result = rfidManager?.RFIDDirectStartInventoryRound(InventoryType.EPC, 100)
                        if (result == ClResult.S_OK.ordinal) {
                            isScanning = true
                            errorMessage = null
                        } else {
                            errorMessage = rfidManager?.GetLastError()
                        }
                    },
                    enabled = !isScanning,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quét", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val result = rfidManager?.RFIDDirectCancelInventoryRound()
                        if (result == ClResult.S_OK.ordinal) {
                            isScanning = false
                        }
                    },
                    enabled = isScanning,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dừng", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        tagList = emptyList()
                        totalReads = 0
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Xóa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                Text(text = "Lỗi: $errorMessage", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Thông tin tổng quan
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Thẻ duy nhất: ${tagList.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                Text(
                    text = "Tổng lượt đọc: $totalReads",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Danh sách thẻ
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (tagList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Chưa có thẻ nào được quét", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tagList, key = { it.epc }) { tag ->
                            RFIDTagListItem(tag)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RFIDTagListItem(tag: RFIDTag) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    ListItem(
        headlineContent = {
            Text(
                text = tag.epc,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "RSSI: ${tag.rssi} dBm", fontSize = 12.sp, color = Color.Gray)
                Text(text = "Số lần: ${tag.count}", fontSize = 12.sp, color = Color.Gray)
                Text(text = sdf.format(Date(tag.timestamp)), fontSize = 12.sp, color = Color.Gray)
            }
        },
        leadingContent = {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}