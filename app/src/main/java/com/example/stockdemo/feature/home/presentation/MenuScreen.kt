package com.example.stockdemo.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockdemo.R
import com.example.stockdemo.core.ui.theme.GreenColor
import com.example.stockdemo.core.ui.theme.OrangeColor
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.core.ui.theme.SecondaryColor
import com.example.stockdemo.core.ui.theme.StockDemoTheme

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
    val userNameFromStore by userViewModel.userName.collectAsState(initial = null)

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
                        text = stringResource(R.string.menu_title),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
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
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.logout),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
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
                androidx.compose.foundation.layout.Row(
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
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(38.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.menu_greeting),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = userName ?: stringResource(R.string.menu_guest),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu_main_features),
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
                            title = stringResource(R.string.menu_import),
                            backgroundColor = GreenColor,
                            onClick = onNavigateToImport
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.ArrowUpward,
                            title = stringResource(R.string.menu_export),
                            backgroundColor = OrangeColor,
                            onClick = onNavigateToExport
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.menu_import_history),
                            backgroundColor = Color(0xFF4CAF50),
                            onClick = onNavigateToImportHistory
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.menu_export_history),
                            backgroundColor = Color(0xFFFF9800),
                            onClick = onNavigateToExportHistory
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.Inventory,
                            title = stringResource(R.string.menu_inventory),
                            backgroundColor = PrimaryColor,
                            onClick = onNavigateToInventory
                        )
                    }
                    item {
                        MenuGridCard(
                            icon = Icons.Default.AutoAwesome,
                            title = stringResource(R.string.menu_chat_ai),
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
            userName = "Nguyen Van A",
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
