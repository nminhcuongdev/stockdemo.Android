package com.example.satostockmanagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cipherlab.rfid.ClResult
import com.cipherlab.rfid.GeneralString
import com.cipherlab.rfid.InventoryType
import com.cipherlab.rfidapi.RfidManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.collections.indexOfFirst
import kotlin.collections.toMutableList
import kotlin.let


class RFIDSample : ComponentActivity() {
    private var rfidManager: RfidManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rfidManager = RfidManager.InitInstance(this)

        setContent {
            MaterialTheme {
                RFIDReaderScreen(rfidManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rfidManager?.Release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RFIDReaderScreen(rfidManager: RfidManager?) {
    var tagList by remember { mutableStateOf<List<RFIDTag>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalTags by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // BroadcastReceiver để nhận dữ liệu RFID
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == GeneralString.Intent_RFIDSERVICE_TAG_DATA) {
                    val response = intent.getIntExtra(GeneralString.EXTRA_RESPONSE, -1)

                    when (response) {
                        GeneralString.RESPONSE_OPERATION_SUCCESS -> {
                            val epc = intent.getStringExtra(GeneralString.EXTRA_EPC) ?: ""
                            val rssi = intent.getIntExtra(GeneralString.EXTRA_DATA_RSSI, 0)
                            val count = intent.getIntExtra(GeneralString.EXTRA_EPC_LENGTH, 1)

                            // Thêm tag mới vào list
                            val newTag = RFIDTag(
                                epc = epc,
                                rssi = rssi,
                                count = count,
                                timestamp = System.currentTimeMillis()
                            )

                            tagList = tagList.toMutableList().apply {
                                // Kiểm tra xem tag đã tồn tại chưa
                                val existingIndex = indexOfFirst { it.epc == epc }
                                if (existingIndex != -1) {
                                    // Cập nhật tag đã tồn tại
                                    this[existingIndex] = this[existingIndex].copy(
                                        count = this[existingIndex].count + 1,
                                        rssi = rssi,
                                        timestamp = System.currentTimeMillis()
                                    )
                                } else {
                                    // Thêm tag mới
                                    add(newTag)
                                }
                            }
                            totalTags++
                        }

                        GeneralString.RESPONSE_OPERATION_FAIL -> {
                            errorMessage = "Đọc thẻ thất bại"
                        }

                        GeneralString.RESPONSE_OPERATION_TIMEOUT_FAIL -> {
                            errorMessage = "Timeout - Không tìm thấy thẻ"
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(GeneralString.Intent_RFIDSERVICE_TAG_DATA)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SATO RFID Reader DEMO") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Thông tin tổng quan
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Tổng số lần đọc: $totalTags",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Số thẻ unique: ${tagList.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút điều khiển
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!isScanning) {
                            // Bắt đầu quét
                            rfidManager?.let { manager ->
                                val result = manager.RFIDDirectStartInventoryRound(
                                    InventoryType.EPC,
                                    100 // Số lần đọc trong một round
                                )
                                if (result == ClResult.S_OK.ordinal) {
                                    isScanning = true
                                    errorMessage = null
                                } else {
                                    errorMessage = manager.GetLastError()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isScanning
                ) {
                    Text("Bắt đầu quét")
                }

                Button(
                    onClick = {
                        if (isScanning) {
                            // Dừng quét
                            rfidManager?.let { manager ->
                                val result = manager.RFIDDirectCancelInventoryRound()
                                if (result == ClResult.S_OK.ordinal) {
                                    isScanning = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isScanning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Dừng")
                }

                Button(
                    onClick = {
                        tagList = emptyList()
                        totalTags = 0
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Xóa")
                }
            }

            // Hiển thị trạng thái
            if (isScanning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang quét...")
                }
            }

            // Hiển thị lỗi nếu có
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Danh sách thẻ RFID
            Text(
                text = "Danh sách thẻ:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tagList, key = { it.epc }) { tag ->
                    RFIDTagItem(tag)
                }
            }
        }
    }
}

@Composable
fun RFIDTagItem(tag: RFIDTag) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "EPC: ${tag.epc}",
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RSSI: ${tag.rssi} dBm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Đọc: ${tag.count} lần",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Thời gian: ${formatTimestamp(tag.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data class cho RFID Tag
data class RFIDTag(
    val epc: String,
    val rssi: Int,
    val count: Int,
    val timestamp: Long
)

// Hàm format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}