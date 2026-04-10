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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.cipherlab.rfid.ClResult
import com.cipherlab.rfid.GeneralString
import com.cipherlab.rfid.InventoryType
import com.cipherlab.rfidapi.RfidManager
import com.example.stockdemo.R
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.feature.stock.domain.model.RFIDTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val rfidManager = remember { RfidManager.InitInstance(context) }

    var tagList by remember { mutableStateOf<List<RFIDTag>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var totalReads by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == GeneralString.Intent_RFIDSERVICE_TAG_DATA) {
                    val response = intent.getIntExtra(GeneralString.EXTRA_RESPONSE, -1)

                    if (response == GeneralString.RESPONSE_OPERATION_SUCCESS) {
                        val epc = intent.getStringExtra(GeneralString.EXTRA_EPC) ?: ""
                        val rssi = intent.getIntExtra(GeneralString.EXTRA_DATA_RSSI, 0)

                        tagList = tagList.toMutableList().apply {
                            val existingIndex = indexOfFirst { it.epc == epc }
                            if (existingIndex != -1) {
                                this[existingIndex] = this[existingIndex].copy(
                                    count = this[existingIndex].count + 1,
                                    rssi = rssi,
                                    timestamp = System.currentTimeMillis()
                                )
                            } else {
                                add(RFIDTag(epc, rssi, 1, System.currentTimeMillis()))
                            }
                        }
                        totalReads++
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val result = rfidManager?.RFIDDirectStartInventoryRound(InventoryType.EPC, 100)
                        if (result == ClResult.S_OK.ordinal) {
                            isScanning = true
                            errorMessage = null
                        } else {
                            errorMessage = rfidManager?.GetLastError()
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
                        val result = rfidManager?.RFIDDirectCancelInventoryRound()
                        if (result == ClResult.S_OK.ordinal) {
                            isScanning = false
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
                        totalReads = 0
                        errorMessage = null
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

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.inventory_unique_tags, tagList.size),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
                Text(
                    text = stringResource(R.string.inventory_total_reads, totalReads),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

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
                            RFIDTagListItem(tag)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RFIDTagListItem(tag: RFIDTag) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    ListItem(
        headlineContent = {
            Text(
                text = tag.epc,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
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
                    text = stringResource(R.string.inventory_count, tag.count),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = sdf.format(Date(tag.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
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
