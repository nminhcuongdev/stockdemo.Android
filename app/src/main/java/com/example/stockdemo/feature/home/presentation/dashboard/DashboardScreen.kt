package com.example.stockdemo.feature.home.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stockdemo.R
import com.example.stockdemo.core.ui.theme.GreenColor
import com.example.stockdemo.core.ui.theme.OrangeColor
import com.example.stockdemo.core.ui.theme.PrimaryColor
import com.example.stockdemo.core.ui.theme.SecondaryColor
import com.example.stockdemo.feature.stock.domain.model.DashboardStats
import com.example.stockdemo.feature.stock.domain.model.LowStockItem

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToImport: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToStocktake: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToImportHistory: () -> Unit,
    onNavigateToExportHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val lowStock by viewModel.lowStock.collectAsStateWithLifecycle()

    DashboardContent(
        userName = userName,
        stats = stats,
        lowStock = lowStock,
        onNavigateToImport = onNavigateToImport,
        onNavigateToExport = onNavigateToExport,
        onNavigateToTransfer = onNavigateToTransfer,
        onNavigateToStocktake = onNavigateToStocktake,
        onNavigateToReport = onNavigateToReport,
        onNavigateToImportHistory = onNavigateToImportHistory,
        onNavigateToExportHistory = onNavigateToExportHistory,
        onLogout = onLogout
    )
}

@Composable
private fun DashboardContent(
    userName: String?,
    stats: DashboardStats,
    lowStock: List<LowStockItem>,
    onNavigateToImport: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToStocktake: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToImportHistory: () -> Unit,
    onNavigateToExportHistory: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader(userName = userName, onLogout = onLogout)

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.dashboard_overview),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Warehouse,
                    value = stats.totalStockItems.toString(),
                    label = stringResource(R.string.dashboard_stat_stock_items),
                    tint = PrimaryColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Numbers,
                    value = stats.totalQuantity.toString(),
                    label = stringResource(R.string.dashboard_stat_total_qty),
                    tint = GreenColor
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Inventory2,
                    value = stats.productCount.toString(),
                    label = stringResource(R.string.dashboard_stat_products),
                    tint = SecondaryColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CloudSync,
                    value = stats.pendingSyncCount.toString(),
                    label = stringResource(R.string.dashboard_stat_pending),
                    tint = OrangeColor
                )
            }

            if (stats.pendingSyncCount > 0) {
                Spacer(Modifier.height(14.dp))
                PendingSyncBanner(count = stats.pendingSyncCount)
            }

            if (lowStock.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                LowStockSection(items = lowStock)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.dashboard_quick_actions),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.ArrowDownward,
                    label = stringResource(R.string.menu_import),
                    tint = GreenColor,
                    onClick = onNavigateToImport
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.ArrowUpward,
                    label = stringResource(R.string.menu_export),
                    tint = OrangeColor,
                    onClick = onNavigateToExport
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.SwapHoriz,
                    label = stringResource(R.string.menu_transfer),
                    tint = AccentBlue,
                    onClick = onNavigateToTransfer
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.History,
                    label = stringResource(R.string.menu_import_history),
                    tint = PrimaryColor,
                    onClick = onNavigateToImportHistory
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.History,
                    label = stringResource(R.string.menu_export_history),
                    tint = OrangeColor,
                    onClick = onNavigateToExportHistory
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.FactCheck,
                    label = stringResource(R.string.menu_stocktake),
                    tint = GreenColor,
                    onClick = onNavigateToStocktake
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Assessment,
                    label = stringResource(R.string.menu_report),
                    tint = SecondaryColor,
                    onClick = onNavigateToReport
                )
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

private val AccentBlue = Color(0xFF5C6BC0)

@Composable
private fun DashboardHeader(userName: String?, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(PrimaryColor, Color(0xFF1565C0))),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.menu_greeting),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Text(
                    text = userName ?: stringResource(R.string.menu_guest),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            IconButton(onClick = onLogout) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = stringResource(R.string.logout),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LowStockSection(items: List<LowStockItem>) {
    Text(
        text = stringResource(R.string.dashboard_low_stock_title),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(14.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            items.forEach { item -> LowStockRow(item) }
        }
    }
}

@Composable
private fun LowStockRow(item: LowStockItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(WarnRed.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Warning, null, tint = WarnRed, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.productName.ifBlank { item.productCode },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(
                    R.string.dashboard_low_stock_levels,
                    item.currentQuantity,
                    item.minQuantity
                ),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.dashboard_low_stock_shortage, item.shortage),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = WarnRed
        )
    }
}

private val WarnRed = Color(0xFFE53935)

@Composable
private fun PendingSyncBanner(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OrangeColor.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CloudSync, null, tint = OrangeColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.dashboard_pending_warning, count),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
