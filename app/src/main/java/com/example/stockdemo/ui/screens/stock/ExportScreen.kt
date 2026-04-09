package com.example.stockdemo.ui.screens.stock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockdemo.domain.model.stock.Stock
import com.example.stockdemo.domain.model.stock.UpdateQuantityRequest
import com.example.stockdemo.ui.screens.login.UserViewModel
import com.example.stockdemo.ui.theme.OrangeColor
import com.example.stockdemo.ui.theme.StockDemoTheme
import com.example.stockdemo.ui.util.toast
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: StockViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userIdFromStore by userViewModel.userId.collectAsState(initial = null)

    // Collect StateFlow thay vì đọc trực tiếp
    val scannedStock by viewModel.scannedStock.collectAsStateWithLifecycle()

    var selectedItem by remember { mutableStateOf<Stock?>(null) }
    var exportQuantity by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    // Toast: collect trên coroutine riêng — không bị block
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            context.toast(message)
        }
    }

    // Tự động mở dialog khi quét được stock qua QR
    LaunchedEffect(scannedStock) {
        scannedStock?.let {
            selectedItem = it
            showDialog = true
            viewModel.clearScannedStock()
        }
    }

    // Lắng nghe QR khi dialog chưa mở
    if (!showDialog) {
        SystemBroadcastReceiver("sato") { intent ->
            val scannedCode = intent?.getStringExtra("data")
            scannedCode?.let { viewModel.getStockByQrCode(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xuất Kho", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading && state.stocks.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null && state.stocks.isEmpty() -> {
                    Text(
                        text = state.error ?: "Lỗi không xác định",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.stocks.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Kho trống", fontSize = 18.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.stocks) { item ->
                            ExportItemCard(
                                stock = item,
                                onClick = {
                                    selectedItem = item
                                    showDialog = true
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (showDialog && selectedItem != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    selectedItem = null
                    exportQuantity = ""
                },
                title = { Text("Xuất: ${selectedItem?.product?.productName}") },
                text = {
                    Column {
                        Text("Mã SP: ${selectedItem?.product?.productCode}")
                        Text("Vị trí: ${selectedItem?.location?.locationName}")
                        Text("Số lượng tồn: ${selectedItem?.quantity} ${selectedItem?.product?.unit}")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = exportQuantity,
                            onValueChange = { exportQuantity = it },
                            label = { Text("Số lượng xuất") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val qty = exportQuantity.toIntOrNull() ?: 0
                            val maxQty = selectedItem?.quantity ?: 0
                            if (qty <= 0 || qty > maxQty) {
                                context.toast("Số lượng không hợp lệ")
                                return@Button
                            }
                            if (userIdFromStore == null) {
                                context.toast("Lỗi: Không tìm thấy ID người dùng!")
                                return@Button
                            }
                            viewModel.updateQuantity(
                                id = selectedItem!!.stockId,
                                updateQuantityRequest = UpdateQuantityRequest(
                                    quantity = qty,
                                    createdBy = userIdFromStore!!
                                )
                            )
                            exportQuantity = ""
                            showDialog = false
                            selectedItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeColor)
                    ) {
                        Text("Xác nhận xuất")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedItem = null
                        exportQuantity = ""
                    }) {
                        Text("Hủy", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun ExportItemCard(
    stock: Stock,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (stock.quantity > 0) Color.White else Color.Gray.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.product?.productName ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Mã: ${stock.product?.productCode}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Vị trí: ${stock.location?.locationName}",
                    fontSize = 13.sp,
                    color = OrangeColor
                )
                Text(
                    text = "Tồn kho: ${stock.quantity} ${stock.product?.unit}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stock.quantity > 0) Color.DarkGray else Color.Red
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExportScreenPreview() {
    StockDemoTheme {
        // ExportScreen layout preview
    }
}