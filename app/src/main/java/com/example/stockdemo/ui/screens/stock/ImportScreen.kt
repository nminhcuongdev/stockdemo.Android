package com.example.stockdemo.ui.screens.stock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.example.stockdemo.domain.model.stock.StockInRequest
import com.example.stockdemo.ui.screens.login.UserViewModel
import com.example.stockdemo.ui.theme.GreenColor
import com.example.stockdemo.ui.theme.PrimaryColor
import com.example.stockdemo.ui.theme.StockDemoTheme
import com.example.stockdemo.ui.util.toast
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: StockViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userIdFromStore by userViewModel.userId.collectAsState(initial = null)

    // Collect StateFlow thay vì đọc trực tiếp từ ViewModel
    val scannedProduct by viewModel.scannedProduct.collectAsStateWithLifecycle()
    val scannedLocation by viewModel.scannedLocation.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }

    // Toast: collect trên coroutine riêng — không bị block bởi các lệnh khác
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            context.toast(message)
        }
    }

    // getStocks() chỉ gọi trong init{} của ViewModel, không cần gọi lại ở đây

    if (showDialog) {
        SystemBroadcastReceiver("sato") { intent ->
            val scannedCode = intent?.getStringExtra("data")
            scannedCode?.let {
                if (isLocationCode(it)) {
                    val code = it.split(";").getOrNull(1) ?: ""
                    viewModel.getLocation(code)
                } else {
                    viewModel.getDObyQrcode(it)
                }
            }
        }
    }

    ImportContent(
        state = state,
        onBack = onBack,
        onAddClick = { showDialog = true },
        scannedProductName = scannedProduct?.product?.productName ?: "",
        scannedPoNumber = scannedProduct?.poNumber ?: "",
        scannedQuantity = scannedProduct?.quantity?.toString() ?: "",
        scannedLocationName = scannedLocation?.locationName ?: "",
        showDialog = showDialog,
        onDismissDialog = {
            showDialog = false
            viewModel.clearScannedProduct()
        },
        onConfirmAdd = {
            if (scannedProduct == null) {
                context.toast("Lỗi: Vui lòng quét QR hàng trước!")
                return@ImportContent
            }
            if (scannedLocation == null) {
                context.toast("Lỗi: Vui lòng quét QR vị trí trước!")
                return@ImportContent
            }
            if (userIdFromStore == null) {
                context.toast("Lỗi: Không tìm thấy ID người dùng!")
                return@ImportContent
            }

            val stockInRequest = StockInRequest(
                locationId = scannedLocation!!.locationId,
                productId = scannedProduct!!.productId,
                quantity = scannedProduct!!.quantity,
                qrCode = scannedProduct!!.qrCode,
                userId = userIdFromStore!!
            )
            viewModel.stockIn(stockInRequest)
            showDialog = false
            viewModel.clearScannedProduct()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContent(
    state: StockUiState,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    scannedProductName: String,
    scannedPoNumber: String,
    scannedQuantity: String,
    scannedLocationName: String,
    showDialog: Boolean,
    onDismissDialog: () -> Unit,
    onConfirmAdd: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nhập Kho") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = GreenColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm", tint = Color.White)
            }
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
                        text = state.error,
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
                        Text("Chưa có hàng hóa nào", fontSize = 18.sp, color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.stocks) { item ->
                            ItemCard(item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = onDismissDialog,
                title = { Text("Thêm Hàng Hóa") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = scannedProductName,
                            onValueChange = {},
                            label = { Text("Tên hàng hóa") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedPoNumber,
                            onValueChange = {},
                            label = { Text("Mã PO") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedQuantity,
                            onValueChange = {},
                            label = { Text("Số lượng") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedLocationName,
                            onValueChange = {},
                            label = { Text("Vị trí") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onConfirmAdd) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun SystemBroadcastReceiver(
    action: String,
    onEvent: (Intent?) -> Unit
) {
    val context = LocalContext.current
    val currentOnEvent by rememberUpdatedState(onEvent)

    DisposableEffect(context, action) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnEvent(intent)
            }
        }
        context.registerReceiver(receiver, IntentFilter(action))
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

fun isLocationCode(input: String?): Boolean {
    if (input.isNullOrBlank()) return false
    return Regex("^LC;.+$").matches(input)
}

@Composable
fun ItemCard(stock: Stock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = stock.product?.productCode ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = stock.product?.productName ?: "",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vị trí: ${stock.location?.locationName}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = PrimaryColor
                )
                Text(
                    text = "Số lượng: ${stock.quantity} ${stock.product?.unit}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GreenColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ImportScreenPreview() {
    StockDemoTheme {
        ImportContent(
            state = StockUiState(stocks = emptyList()),
            onBack = {},
            onAddClick = {},
            scannedProductName = "",
            scannedPoNumber = "",
            scannedQuantity = "",
            scannedLocationName = "",
            showDialog = false,
            onDismissDialog = {},
            onConfirmAdd = {}
        )
    }
}