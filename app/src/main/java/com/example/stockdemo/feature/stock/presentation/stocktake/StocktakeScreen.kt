package com.example.stockdemo.feature.stock.presentation.stocktake

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockdemo.R
import com.example.stockdemo.core.ui.theme.GreenColor
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.core.ui.util.toast
import com.example.stockdemo.feature.stock.domain.model.Location
import com.example.stockdemo.feature.stock.domain.model.StockTake
import com.example.stockdemo.feature.stock.domain.model.StockTakeCountLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StocktakeScreen(
    viewModel: StocktakeViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    val counts = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StocktakeEvent.Reconciled -> {
                    context.toast(context.getString(R.string.stocktake_success))
                    counts.clear()
                }
                is StocktakeEvent.Error -> context.toast(event.message.asString(context))
            }
        }
    }

    val countedItems = uiState.products.mapNotNull { product ->
        counts[product.productId]?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
            ?.let { StockTakeCountLine(product.productId, it) }
    }
    val canSubmit = selectedLocation != null && countedItems.isNotEmpty() && !uiState.isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stocktake_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            LocationDropdown(
                selected = selectedLocation,
                locations = uiState.locations,
                onSelected = { selectedLocation = it }
            )

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.stocktake_count_section),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            uiState.products.forEach { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(product.productName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(product.productCode, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedTextField(
                        value = counts[product.productId] ?: "",
                        onValueChange = { new -> counts[product.productId] = new.filter { it.isDigit() } },
                        placeholder = { Text(stringResource(R.string.stocktake_count_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val loc = selectedLocation ?: return@Button
                    viewModel.submitCount(loc.locationId, null, countedItems)
                },
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isSubmitting && uiState.review == null) {
                    CircularProgressIndicator(modifier = Modifier.height(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.FactCheck, contentDescription = null)
                    Text("  " + stringResource(R.string.stocktake_reconcile), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    uiState.review?.let { session ->
        ReviewDialog(
            session = session,
            isSubmitting = uiState.isSubmitting,
            onConfirm = viewModel::confirmReconcile,
            onDismiss = viewModel::dismissReview
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDropdown(
    selected: Location?,
    locations: List<Location>,
    onSelected: (Location) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { "${it.locationCode} - ${it.locationName}" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.stocktake_location)) },
            placeholder = { Text(stringResource(R.string.stocktake_select_location)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = { Text("${location.locationCode} - ${location.locationName}") },
                    onClick = {
                        onSelected(location)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewDialog(
    session: StockTake,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(R.string.stocktake_review_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.stocktake_col_product), Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.stocktake_col_system), Modifier.width(48.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.stocktake_col_counted), Modifier.width(48.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.stocktake_col_variance), Modifier.width(56.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                session.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.productName, Modifier.weight(1f), fontSize = 13.sp)
                        Text("${item.systemQuantity}", Modifier.width(48.dp), fontSize = 13.sp)
                        Text("${item.countedQuantity}", Modifier.width(48.dp), fontSize = 13.sp)
                        val varColor = when {
                            item.variance < 0 -> Color(0xFFE53935)
                            item.variance > 0 -> GreenColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = if (item.variance > 0) "+${item.variance}" else "${item.variance}",
                            modifier = Modifier.width(56.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = varColor
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.stocktake_confirm_adjust))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
