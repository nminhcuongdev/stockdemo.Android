package com.example.stockdemo.feature.stock.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockdemo.R
import com.example.stockdemo.core.ui.theme.OrangeColor
import com.example.stockdemo.core.ui.theme.StockDemoTheme
import com.example.stockdemo.core.ui.util.toast
import com.example.stockdemo.feature.home.presentation.UserViewModel
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.UpdateQuantityRequest
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
    val scannedStock by viewModel.scannedStock.collectAsStateWithLifecycle()

    var selectedItem by remember { mutableStateOf<Stock?>(null) }
    var exportQuantity by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            context.toast(message.asString(context))
        }
    }

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
            scannedCode?.let { viewModel.getStockByQrCode(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                        text = state.error?.asString(context) ?: stringResource(R.string.unknown_error),
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
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.export_empty),
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
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
            val productName = selectedItem?.product?.productName.orEmpty()
            val productCode = selectedItem?.product?.productCode.orEmpty()
            val locationName = selectedItem?.location?.locationName.orEmpty()
            val quantity = selectedItem?.quantity?.toString().orEmpty()
            val unit = selectedItem?.product?.unit.orEmpty()

            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    selectedItem = null
                    exportQuantity = ""
                },
                title = {
                    Text(stringResource(R.string.export_dialog_title, productName))
                },
                text = {
                    Column {
                        Text(stringResource(R.string.export_product_code, productCode))
                        Text(stringResource(R.string.export_location, locationName))
                        Text(stringResource(R.string.export_stock_quantity, quantity, unit))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = exportQuantity,
                            onValueChange = { exportQuantity = it },
                            label = { Text(stringResource(R.string.export_quantity_label)) },
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
                                context.toast(context.getString(R.string.export_invalid_quantity))
                                return@Button
                            }
                            val userId = userIdFromStore
                            if (userId == null) {
                                context.toast(context.getString(R.string.user_id_missing))
                                return@Button
                            }
                            viewModel.updateQuantity(
                                id = selectedItem!!.stockId,
                                updateQuantityRequest = UpdateQuantityRequest(
                                    quantity = qty,
                                    createdBy = userId
                                )
                            )
                            exportQuantity = ""
                            showDialog = false
                            selectedItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeColor)
                    ) {
                        Text(stringResource(R.string.export_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            selectedItem = null
                            exportQuantity = ""
                        }
                    ) {
                        Text(stringResource(R.string.cancel), color = Color.Gray)
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
                    text = stock.product?.productName.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(
                        R.string.export_item_code,
                        stock.product?.productCode.orEmpty()
                    ),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = stringResource(
                        R.string.export_location,
                        stock.location?.locationName.orEmpty()
                    ),
                    fontSize = 13.sp,
                    color = OrangeColor
                )
                Text(
                    text = stringResource(
                        R.string.export_item_stock,
                        stock.quantity.toString(),
                        stock.product?.unit.orEmpty()
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (stock.quantity > 0) Color.DarkGray else Color.Red
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
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
    }
}
