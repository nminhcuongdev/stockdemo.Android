package com.example.stockdemo.feature.stock.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cipherlab.rfid.ClResult
import com.cipherlab.rfid.Gen2Settings
import com.cipherlab.rfid.GeneralString
import com.cipherlab.rfid.InventoryStatusSettings
import com.cipherlab.rfid.QValue
import com.cipherlab.rfid.SLFlagSettings
import com.cipherlab.rfid.ScanMode
import com.cipherlab.rfid.SessionSettings
import com.cipherlab.rfid.WorkMode
import com.cipherlab.rfidapi.RfidManager
import com.example.stockdemo.R
import com.example.stockdemo.core.rfid.RfidPreferences
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.feature.stock.domain.model.RFIDTag
import com.example.stockdemo.feature.stock.domain.model.Stock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    settingsViewModel: RfidSettingsViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val rfidManager = remember { RfidManager.InitInstance(context) }

    val txPower by settingsViewModel.txPower.collectAsStateWithLifecycle()
    val sessionPref by settingsViewModel.session.collectAsStateWithLifecycle()
    val qDynamicPref by settingsViewModel.qDynamic.collectAsStateWithLifecycle()
    val workModePref by settingsViewModel.workMode.collectAsStateWithLifecycle()
    val filterDuplicatePref by settingsViewModel.filterDuplicate.collectAsStateWithLifecycle()

    val availableStocks by inventoryViewModel.stocks.collectAsStateWithLifecycle()
    val epcToStock by inventoryViewModel.epcToStock.collectAsStateWithLifecycle()
    var assigningEpc by remember { mutableStateOf<String?>(null) }

    var tagList by remember { mutableStateOf<List<RFIDTag>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == GeneralString.Intent_RFIDSERVICE_TAG_DATA) {
                    val response = intent.getIntExtra(GeneralString.EXTRA_RESPONSE, -1)

                    if (response == GeneralString.RESPONSE_OPERATION_SUCCESS) {
                        val epc = intent.getStringExtra(GeneralString.EXTRA_EPC) ?: ""
                        val rssi = intent.getIntExtra(GeneralString.EXTRA_DATA_RSSI, 0)

                        // Only track EPCs that are mapped to a known product (server-side
                        // EpcMappings): unmapped reads are ignored entirely, not shown or counted.
                        // App-level dedup against everything read so far this session (tagList
                        // isn't cleared by Stop/Scan, only by the Xóa button), independent of the
                        // reader's own hardware filter: an EPC already seen is simply ignored.
                        if (epcToStock.containsKey(epc)) {
                            val alreadySeen = tagList.any { it.epc == epc }
                            if (!alreadySeen) {
                                tagList = tagList + RFIDTag(epc, rssi, 1, System.currentTimeMillis())
                            }
                        }
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
                title = { Text(stringResource(R.string.inventory_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        try {
                            // Apply the user's configured defaults (from the Settings screen) right
                            // before scanning, when the RFID service is guaranteed to be bound.
                            rfidManager?.SetWorkMode(workModeFromName(workModePref))
                            rfidManager?.SetGen2(
                                Gen2Settings().apply {
                                    Session = sessionFromName(sessionPref)
                                    InventoryStatus_Action = InventoryStatusSettings.AB_FLIP
                                    SL_Flag = SLFlagSettings.Asserted
                                }
                            )
                            rfidManager?.SetQValue(
                                QValue().apply {
                                    Dynamic = qDynamicPref
                                    value = 4
                                    Min = 0
                                    Max = 15
                                }
                            )
                            rfidManager?.SetTxPower(txPower)
                            // Duplicate filter: when on, the reader reports each tag once instead
                            // of streaming the same EPC repeatedly. Clear its records first so a new
                            // scan session re-detects tags that were seen in a previous session.
                            rfidManager?.ClearFilterDuplicate()
                            rfidManager?.SetFilterDuplicate(if (filterDuplicatePref) 1 else 0)
                            // Continuous scan: emulate holding the trigger. The reader then keeps
                            // inventorying in hardware (no app-side round gaps) until we release it
                            // in Stop. This is the proper "read many tags until stop" mechanism;
                            // RFIDDirectStartInventoryRound only does one short automated round.
                            rfidManager?.SetScanMode(ScanMode.Continuous)
                            val result = rfidManager?.SoftScanTrigger(true)
                            if (result == ClResult.S_OK.ordinal) {
                                isScanning = true
                                errorMessage = null
                            } else {
                                errorMessage = rfidManager?.GetLastError()
                            }
                        } catch (e: Throwable) {
                            errorMessage = e.message
                        }
                    },
                    enabled = !isScanning,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.scan), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        try {
                            // Release the emulated trigger to stop continuous scanning.
                            val result = rfidManager?.SoftScanTrigger(false)
                            if (result == ClResult.S_OK.ordinal) {
                                isScanning = false
                            }
                        } catch (e: Throwable) {
                            isScanning = false
                            errorMessage = e.message
                        }
                    },
                    enabled = isScanning,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.stop), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        tagList = emptyList()
                        errorMessage = null
                        // Forget the reader's duplicate records so cleared tags can be read again.
                        try {
                            rfidManager?.ClearFilterDuplicate()
                        } catch (_: Throwable) {
                        }
                    },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.delete), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (errorMessage != null) {
                Text(
                    text = stringResource(R.string.inventory_error, errorMessage.orEmpty()),
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Direct power (Tx) control. Dragging updates the label live; releasing persists it
            // and, if a scan is in progress, applies it to the reader immediately.
            var powerSlider by remember(txPower) { mutableFloatStateOf(txPower.toFloat()) }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.inventory_power, powerSlider.toInt()),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Slider(
                value = powerSlider,
                onValueChange = { powerSlider = it },
                onValueChangeFinished = {
                    val newPower = powerSlider.toInt()
                    settingsViewModel.setTxPower(newPower)
                    if (isScanning) {
                        try {
                            rfidManager?.SetTxPower(newPower)
                        } catch (e: Throwable) {
                            errorMessage = e.message
                        }
                    }
                },
                valueRange = RfidPreferences.MIN_TX_POWER.toFloat()..RfidPreferences.MAX_TX_POWER.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.inventory_total, tagList.size),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (tagList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.inventory_empty),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tagList, key = { it.epc }) { tag ->
                            // Defensive: tagList should only ever hold mapped EPCs, but skip
                            // rendering if a mapping was removed elsewhere after being added here.
                            val stock = epcToStock[tag.epc]
                            if (stock != null) {
                                RFIDTagListItem(
                                    tag = tag,
                                    stock = stock,
                                    onAssignClick = { assigningEpc = tag.epc }
                                )
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    val epcBeingAssigned = assigningEpc
    if (epcBeingAssigned != null) {
        AssignProductDialog(
            epc = epcBeingAssigned,
            stocks = availableStocks,
            onRefresh = inventoryViewModel::refreshStocks,
            onSelect = { stock ->
                inventoryViewModel.assignEpc(epcBeingAssigned, stock.qrCode)
                assigningEpc = null
            },
            onDismiss = { assigningEpc = null }
        )
    }
}

@Composable
private fun AssignProductDialog(
    epc: String,
    stocks: List<Stock>,
    onRefresh: () -> Unit,
    onSelect: (Stock) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.inventory_assign_title)) },
        text = {
            Column {
                Text(
                    text = epc,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (stocks.isEmpty()) {
                    Text(
                        text = stringResource(R.string.inventory_assign_no_stocks),
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onRefresh) {
                        Text(stringResource(R.string.retry))
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(320.dp)) {
                        items(stocks, key = { it.stockId }) { stock ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(stock) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = stock.product?.productName ?: stock.qrCode,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = stringResource(
                                        R.string.inventory_assign_stock_subtitle,
                                        stock.product?.productCode ?: "-",
                                        stock.location?.locationName ?: "-",
                                        stock.quantity
                                    ),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Map the persisted setting names back to SDK enums. Unknown values fall back to the
// validated defaults so a bad stored value can never crash scanning.
private fun sessionFromName(name: String): SessionSettings = when (name) {
    "S0" -> SessionSettings.S0
    "S1" -> SessionSettings.S1
    "S2" -> SessionSettings.S2
    "S3" -> SessionSettings.S3
    else -> SessionSettings.S1
}

private fun workModeFromName(name: String): WorkMode = when (name) {
    "MultiTagMode" -> WorkMode.MultiTagMode
    "ComprehensiveMode" -> WorkMode.ComprehensiveMode
    "SingleTagMode" -> WorkMode.SingleTagMode
    else -> WorkMode.MultiTagMode
}

@Composable
fun RFIDTagListItem(
    tag: RFIDTag,
    stock: Stock,
    onAssignClick: () -> Unit = {}
) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Every tag reaching this list is already mapped (unmapped EPCs are filtered out before
    // being added), so this always shows product info. Tapping re-assigns to a different product.
    ListItem(
        modifier = Modifier.clickable(onClick = onAssignClick),
        headlineContent = {
            Text(
                text = stock.product?.productName ?: stock.qrCode,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = stringResource(
                        R.string.inventory_assign_stock_subtitle,
                        stock.product?.productCode ?: "-",
                        stock.location?.locationName ?: "-",
                        stock.quantity
                    ),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.inventory_rssi, tag.rssi),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = sdf.format(Date(tag.timestamp)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = PrimaryColor,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}
