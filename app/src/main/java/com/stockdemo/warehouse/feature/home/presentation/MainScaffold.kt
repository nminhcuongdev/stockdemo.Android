package com.stockdemo.warehouse.feature.home.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.stockdemo.warehouse.R
import com.stockdemo.warehouse.feature.chat.presentation.ChatAIScreen
import com.stockdemo.warehouse.feature.chat.presentation.ChatViewModel
import com.stockdemo.warehouse.feature.home.presentation.dashboard.DashboardScreen
import com.stockdemo.warehouse.feature.home.presentation.dashboard.DashboardViewModel
import com.stockdemo.warehouse.feature.stock.presentation.InventoryScreen

private sealed class HomeTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    data object Dashboard : HomeTab("home/dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard)
    data object Inventory : HomeTab("home/inventory", R.string.nav_inventory, Icons.Filled.Inventory)
    data object Chat : HomeTab("home/chat", R.string.nav_chat, Icons.AutoMirrored.Filled.Chat)
    data object Settings : HomeTab("home/settings", R.string.nav_settings, Icons.Filled.Settings)
}

private val homeTabs = listOf(
    HomeTab.Dashboard,
    HomeTab.Inventory,
    HomeTab.Chat,
    HomeTab.Settings
)

/**
 * The logged-in home experience: a Material 3 bottom navigation bar with a nested
 * NavHost. Secondary flows (import/export, history) are pushed onto the outer
 * navigation graph via the provided callbacks.
 */
@Composable
fun MainScaffold(
    onNavigateToImport: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToStocktake: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToImportHistory: () -> Unit,
    onNavigateToExportHistory: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                homeTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = HomeTab.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(HomeTab.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
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
            composable(HomeTab.Inventory.route) {
                InventoryScreen(onBack = {}, showBackButton = false)
            }
            composable(HomeTab.Chat.route) {
                val chatViewModel: ChatViewModel = hiltViewModel()
                ChatAIScreen(viewModel = chatViewModel, onBack = {}, showBackButton = false)
            }
            composable(HomeTab.Settings.route) {
                val userViewModel: UserViewModel = hiltViewModel()
                SettingsScreen(
                    userViewModel = userViewModel,
                    onBack = {},
                    showBackButton = false
                )
            }
        }
    }
}
