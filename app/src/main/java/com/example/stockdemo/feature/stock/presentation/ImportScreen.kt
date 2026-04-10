package com.example.stockdemo.feature.stock.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.example.stockdemo.core.ui.theme.GreenColor
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.core.ui.theme.StockDemoTheme
import com.example.stockdemo.core.ui.util.toast
import com.example.stockdemo.feature.home.presentation.UserViewModel
import com.example.stockdemo.feature.stock.domain.model.Stock
import com.example.stockdemo.feature.stock.domain.model.StockInRequest
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
    val scannedProduct by viewModel.scannedProduct.collectAsStateWithLifecycle()
    val scannedLocation by viewModel.scannedLocation.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            context.toast(message)
        }
    }

    if (showDialog) {
        SystemBroadcastReceiver("sato") { intent ->
            val scannedCode = intent?.getStringExtra("data")
            scannedCode?.let {
                if (isLocationCode(it)) {
                    val code = it.split(";").getOrNull(1) ?: ""
                    viewModel.getLocation(code)
                } else {
                    viewModel.getDeliveryOrderByQrCode(it)
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
            val product = scannedProduct
            if (product == null) {
                context.toast(context.getString(R.string.import_scan_product_required))
                return@ImportContent
            }
            val location = scannedLocation
            if (location == null) {
                context.toast(context.getString(R.string.import_scan_location_required))
                return@ImportContent
            }
            val userId = userIdFromStore
            if (userId == null) {
                context.toast(context.getString(R.string.user_id_missing))
                return@ImportContent
            }

            val stockInRequest = StockInRequest(
                locationId = location.locationId,
                productId = product.productId,
                quantity = product.quantity,
                qrCode = product.qrCode,
                userId = userId
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
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.import_add_desc),
                    tint = Color.White
                )
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
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.import_empty),
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
                title = { Text(stringResource(R.string.import_dialog_title)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = scannedProductName,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.import_product_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedPoNumber,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.import_po_code)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedQuantity,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.import_quantity)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = scannedLocationName,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.import_location)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = onConfirmAdd) {
                        Text(stringResource(R.string.add))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDialog) {
                        Text(stringResource(R.string.cancel))
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
                    text = stock.product?.productCode.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = stock.product?.productName.orEmpty(),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.label_location,
                        stock.location?.locationName.orEmpty()
                    ),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = PrimaryColor
                )
                Text(
                    text = stringResource(
                        R.string.label_quantity,
                        stock.quantity.toString(),
                        stock.product?.unit.orEmpty()
                    ),
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
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
