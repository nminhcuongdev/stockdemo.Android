package com.example.stockdemo.ui.screens.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.stockdemo.domain.model.stock.StockIn
import com.example.stockdemo.ui.theme.GreenColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportHistoryScreen(
    viewModel: ImportHistoryViewModel,
    onBack: () -> Unit
) {
    val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch Sử Nhập Kho", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenColor,
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
                .background(Color(0xFFF5F7FA))
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.stockInId },
                    contentType = pagingItems.itemContentType { "stockIn" }
                ) { index ->
                    pagingItems[index]?.let { item ->
                        ImportHistoryItem(item)
                    }
                }

                // Handle Load States
                val loadState = pagingItems.loadState
                
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                    loadState.refresh is LoadState.Error -> {
                        val error = loadState.refresh as LoadState.Error
                        item {
                            ErrorItem(
                                message = error.error.localizedMessage ?: "Lỗi tải dữ liệu",
                                onRetry = { pagingItems.retry() }
                            )
                        }
                    }
                    loadState.append is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp).align(Alignment.Center))
                            }
                        }
                    }
                    loadState.append is LoadState.Error -> {
                        val error = loadState.append as LoadState.Error
                        item {
                            ErrorItem(
                                message = error.error.localizedMessage ?: "Lỗi tải thêm dữ liệu",
                                onRetry = { pagingItems.retry() }
                            )
                        }
                    }
                    loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0 -> {
                        item {
                            EmptyState()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportHistoryItem(item: StockIn) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.product?.productName ?: "Sản phẩm không xác định",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2C3E50),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = GreenColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+${item.quantity} ${item.product?.unit ?: ""}",
                        color = GreenColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 14.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(14.dp), 
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Mã: ${item.product?.productCode ?: "N/A"}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Text(
                    text = "Vị trí: ",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Text(
                    text = item.location?.locationName ?: "N/A",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF34495E)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Người nhập: ${item.user?.fullName ?: "N/A"}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = formatDateTime(item.createdDate),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ErrorItem(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = Color.Red, fontSize = 14.sp)
        TextButton(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chưa có lịch sử nhập", fontSize = 18.sp, color = Color.Gray)
    }
}

private fun formatDateTime(dateTimeString: String?): String {
    if (dateTimeString.isNullOrBlank()) return ""
    return try {
        // Handle ISO format commonly returned by APIs (e.g., 2023-10-27T10:00:00)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        if (date != null) outputFormat.format(date) else dateTimeString
    } catch (e: Exception) {
        dateTimeString
    }
}
