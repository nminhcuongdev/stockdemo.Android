package com.example.stockdemo.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockdemo.ui.theme.*

@Composable
fun MenuScreen(
    userViewModel: UserViewModel,
    onNavigateToImport: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToImportHistory: () -> Unit,
    onNavigateToExportHistory: () -> Unit,
    onNavigateToChatAI: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val userNameFromStore by userViewModel.userName.collectAsState(initial = "Đang tải...")

    MenuContent(
        userName = userNameFromStore,
        onNavigateToImport = onNavigateToImport,
        onNavigateToExport = onNavigateToExport,
        onNavigateToInventory = onNavigateToInventory,
        onNavigateToImportHistory = onNavigateToImportHistory,
        onNavigateToExportHistory = onNavigateToExportHistory,
        onNavigateToChatAI = onNavigateToChatAI,
        onNavigateToSettings = onNavigateToSettings,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuContent(
    userName: String?,
    onNavigateToImport: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToImportHistory: () -> Unit,
    onNavigateToExportHistory: () -> Unit,
    onNavigateToChatAI: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "QUẢN LÝ KHO", 
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 16.dp,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFF1F1),
                            contentColor = Color.Red
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đăng Xuất", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryColor, Color(0xFF1565C0))
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(38.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Xin chào,",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = userName ?: "Khách hàng",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Dashboard Grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Chức năng chính",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        MenuGridCard(
                            icon = Icons.Default.ArrowDownward,
                            title = "Nhập Kho",
                            backgroundColor = GreenColor,
                            onClick = onNavigateToImport
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.ArrowUpward,
                            title = "Xuất Kho",
                            backgroundColor = OrangeColor,
                            onClick = onNavigateToExport
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.History,
                            title = "Lịch Sử Nhập",
                            backgroundColor = Color(0xFF4CAF50),
                            onClick = onNavigateToImportHistory
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.History,
                            title = "Lịch Sử Xuất",
                            backgroundColor = Color(0xFFFF9800),
                            onClick = onNavigateToExportHistory
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.Inventory,
                            title = "Kiểm Kê",
                            backgroundColor = PrimaryColor,
                            onClick = onNavigateToInventory
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.AutoAwesome,
                            title = "Chat AI",
                            backgroundColor = SecondaryColor,
                            onClick = onNavigateToChatAI
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGridCard(
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(backgroundColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = backgroundColor
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34495E),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    StockDemoTheme {
        MenuContent(
            userName = "Nguyễn Văn A",
            onNavigateToImport = {},
            onNavigateToExport = {},
            onNavigateToInventory = {},
            onNavigateToImportHistory = {},
            onNavigateToExportHistory = {},
            onNavigateToChatAI = {},
            onNavigateToSettings = {},
            onLogout = {}
        )
    }
}
