package com.example.satostockmanagement.screens

import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satostockmanagement.helper.SystemBroadcastReceiver
import com.example.satostockmanagement.models.stocks.Stock
import com.example.satostockmanagement.models.stocks.UpdateQuantityRequest
import com.example.satostockmanagement.ui.theme.OrangeColor
import com.example.satostockmanagement.viewmodel.stock.StockViewModel
import com.example.satostockmanagement.viewmodel.user.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.text.split

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: StockViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val userIdFromStore by userViewModel.userId.collectAsState(initial = null)
    val stocks = viewModel.stocks
    val scannedStock = viewModel.scannedStock
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    var selectedItem by remember { mutableStateOf<Stock?>(null) }
    var exportQuantity by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getStocks()

        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Tự động mở dialog khi quét được stock
    LaunchedEffect(scannedStock) {
        scannedStock?.let {
            selectedItem = it
            showDialog = true
            viewModel.clearScannedStock()
        }
    }

    if (!showDialog) {
        SystemBroadcastReceiver("sato") { intent ->
            val scannedCode = intent?.getStringExtra("data")
            scannedCode?.let {
                viewModel.getStockByQrCode(scannedCode)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xuất Kho") },
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
            if (stocks.isEmpty()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
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
                        Text(
                            "Kho trống",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(stocks) { item ->
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
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
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
                        Text("ID Stock: ${selectedItem?.stockId}")
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
                            if (qty > 0 && qty <= (selectedItem?.quantity ?: 0)) {
                                if (userIdFromStore == null) {
                                    Toast.makeText(context, "Lỗi: Không tìm thấy ID người dùng!", Toast.LENGTH_SHORT).show()
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
                            } else {
                                Toast.makeText(context, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Xuất kho")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedItem = null
                        exportQuantity = ""
                    }) {
                        Text("Hủy")
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
        colors = CardDefaults.cardColors(
            containerColor = if (stock.quantity > 0) Color.White else Color.Gray.copy(alpha = 0.3f)
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
                    text = stock.product.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Tồn kho: ${stock.quantity} ${stock.product.unit}",
                    fontSize = 14.sp,
                    color = if (stock.quantity > 0) Color.Gray else Color.Red
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}