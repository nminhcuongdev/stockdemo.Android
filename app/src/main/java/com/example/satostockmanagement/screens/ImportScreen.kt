package com.example.satostockmanagement.screens

import android.widget.Toast
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
import com.example.satostockmanagement.models.stocks.StockInRequest
import com.example.satostockmanagement.ui.theme.GreenColor
import com.example.satostockmanagement.viewmodel.stock.StockViewModel
import com.example.satostockmanagement.viewmodel.user.UserViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: StockViewModel,
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getStocks()

        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val userIdFromStore by userViewModel.userId.collectAsState(initial = null)

    var stocks = viewModel.stocks
    var scannedProduct = viewModel.scannedProduct
    var scannedLocation = viewModel.scannedLocation
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage


    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SystemBroadcastReceiver("sato") { intent ->
            val scannedCode = intent?.getStringExtra("data")
            scannedCode?.let {
                if (isLocationCode(scannedCode)) {
                    val parts = scannedCode.split(";")
                    val prefix = parts.get(0) // "LC"
                    val code = parts.get(1)   // "A-01"
                    viewModel.getLocation(code)
                } else {
                    viewModel.getDObyQrcode(scannedCode)
                }
            }
        }
    }

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
                onClick = { showDialog = true },
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
            if (stocks.isEmpty()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
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
                            "Chưa có hàng hóa nào",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(stocks) { item ->
                            ItemCard(item)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Thêm Hàng Hóa") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = scannedProduct?.product?.productName ?: "",
                            onValueChange = { },
                            label = { Text("Tên hàng hóa") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedProduct?.poNumber ?: "",
                            onValueChange = { },
                            label = { Text("Mã PO") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedProduct?.quantity?.toString() ?: "",
                            onValueChange = { },
                            label = { Text("Số lượng") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedLocation?.locationName ?: "",
                            onValueChange = { },
                            label = { Text("Vị trí") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {

                            if (scannedProduct == null) {
                                Toast.makeText(context, "Lỗi: Vui lòng quét QR hàng trước!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (scannedLocation == null) {
                                Toast.makeText(context, "Lỗi: Vui lòng quét QR ví trí trước!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (userIdFromStore == null) {
                                Toast.makeText(context, "Lỗi: Không tìm thấy ID người dùng!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val stockInRequest = StockInRequest(
                                locationId = scannedLocation.locationId,
                                productId = scannedProduct.productId,
                                quantity = scannedProduct.quantity,
                                qrCode = scannedProduct.qrCode,
                                userId = userIdFromStore!!
                            )
                            viewModel.stockIn(stockInRequest)
                            showDialog = false
                            viewModel.clearScannedProduct()
                        }
                    ) {
                        Text("Thêm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        viewModel.clearScannedProduct()
                    }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

fun isLocationCode(input: String?): Boolean {
    if (input.isNullOrBlank()) return false
    val regex = Regex("^LC;.+$")

    return regex.matches(input)
}

@Composable
fun ItemCard(stock: Stock) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.product.productCode,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = stock.product.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = "Vị trí: ${stock.location.locationName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                )
                Text(
                    text = "Số lượng: ${stock.quantity} ${stock.product.unit}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GreenColor
            )
        }
    }
}