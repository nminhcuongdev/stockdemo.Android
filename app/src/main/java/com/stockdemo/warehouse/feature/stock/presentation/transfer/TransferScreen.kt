package com.stockdemo.warehouse.feature.stock.presentation.transfer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.stockdemo.warehouse.R
import com.stockdemo.warehouse.core.ui.theme.PrimaryColor
import com.stockdemo.warehouse.core.ui.util.toast
import com.stockdemo.warehouse.feature.stock.domain.model.Location
import com.stockdemo.warehouse.feature.stock.domain.model.Stock
import com.stockdemo.warehouse.feature.stock.presentation.SystemBroadcastReceiver
import com.stockdemo.warehouse.feature.stock.presentation.isLocationCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: TransferViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedStock by remember { mutableStateOf<Stock?>(null) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var quantityText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TransferEvent.Success -> {
                    context.toast(context.getString(R.string.transfer_success))
                    selectedStock = null
                    selectedLocation = null
                    quantityText = ""
                }
                is TransferEvent.Error -> context.toast(event.message.asString(context))
            }
        }
    }

    // Listen for hardware QR/barcode scans (same broadcast the scanner engine emits for Import).
    SystemBroadcastReceiver("mici") { intent ->
        val scannedCode = intent?.getStringExtra("data")
        if (!scannedCode.isNullOrBlank() && !isLocationCode(scannedCode)) {
            val match = uiState.stocks.firstOrNull { it.qrCode == scannedCode }
            if (match != null) {
                selectedStock = match
                if (selectedLocation?.locationId == match.locationId) selectedLocation = null
            } else {
                context.toast(context.getString(R.string.transfer_source_not_found))
            }
        }
    }

    val quantity = quantityText.toIntOrNull()
    val availableLocations = uiState.locations.filter { it.locationId != selectedStock?.locationId }
    val canSubmit = selectedStock != null &&
        selectedLocation != null &&
        quantity != null &&
        quantity > 0 &&
        quantity <= (selectedStock?.quantity ?: 0) &&
        !uiState.isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transfer_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Source stock — selected by scanning its QR code (hardware scanner trigger).
            Text(
                text = stringResource(R.string.transfer_source),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val stock = selectedStock
                    Text(
                        text = stock?.let { stockLabel(it) } ?: stringResource(R.string.transfer_select_source),
                        color = if (stock != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (stock != null) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (stock != null) {
                        IconButton(onClick = { selectedStock = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.transfer_rescan)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = stringResource(R.string.scan),
                            tint = PrimaryColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Destination location
            DropdownField(
                label = stringResource(R.string.transfer_target_location),
                selectedText = selectedLocation?.let { locationLabel(it) } ?: "",
                placeholder = stringResource(R.string.transfer_select_location),
                options = availableLocations,
                optionLabel = { locationLabel(it) },
                onSelected = { selectedLocation = it },
                enabled = selectedStock != null
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = { new -> quantityText = new.filter { it.isDigit() } },
                label = { Text(stringResource(R.string.transfer_quantity)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = selectedStock?.let {
                    { Text(stringResource(R.string.transfer_available, it.quantity)) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val stock = selectedStock ?: return@Button
                    val location = selectedLocation ?: return@Button
                    val qty = quantity ?: return@Button
                    viewModel.transfer(stock.stockId, location.locationId, qty)
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text(
                        text = "  " + stringResource(R.string.transfer_submit),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun stockLabel(stock: Stock): String {
    val name = stock.product?.productName ?: stock.qrCode
    val loc = stock.location?.locationCode ?: stock.locationId.toString()
    return "$name • $loc • SL: ${stock.quantity}"
}

private fun locationLabel(location: Location): String =
    "${location.locationCode} - ${location.locationName}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    selectedText: String,
    placeholder: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.transfer_no_options), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
