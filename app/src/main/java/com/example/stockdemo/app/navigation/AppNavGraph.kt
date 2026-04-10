package com.example.stockdemo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stockdemo.feature.auth.presentation.login.LoginScreen
import com.example.stockdemo.feature.auth.presentation.login.LoginViewModel
import com.example.stockdemo.feature.chat.presentation.ChatAIScreen
import com.example.stockdemo.feature.chat.presentation.ChatViewModel
import com.example.stockdemo.feature.home.presentation.MenuScreen
import com.example.stockdemo.feature.home.presentation.SettingsScreen
import com.example.stockdemo.feature.home.presentation.UserViewModel
import com.example.stockdemo.feature.stock.presentation.ExportHistoryScreen
import com.example.stockdemo.feature.stock.presentation.ExportHistoryViewModel
import com.example.stockdemo.feature.stock.presentation.ExportScreen
import com.example.stockdemo.feature.stock.presentation.ImportHistoryScreen
import com.example.stockdemo.feature.stock.presentation.ImportHistoryViewModel
import com.example.stockdemo.feature.stock.presentation.ImportScreen
import com.example.stockdemo.feature.stock.presentation.InventoryScreen
import com.example.stockdemo.feature.stock.presentation.StockViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
        modifier = modifier
    ) {
        composable(AppDestination.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                userViewModel = userViewModel,
                onLoginSuccess = {
                    navController.navigate(AppDestination.Menu.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Menu.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            MenuScreen(
                userViewModel = userViewModel,
                onNavigateToImport = { navController.navigate(AppDestination.Import.route) },
                onNavigateToExport = { navController.navigate(AppDestination.Export.route) },
                onNavigateToInventory = { navController.navigate(AppDestination.Inventory.route) },
                onNavigateToImportHistory = { navController.navigate(AppDestination.ImportHistory.route) },
                onNavigateToExportHistory = { navController.navigate(AppDestination.ExportHistory.route) },
                onNavigateToChatAI = { navController.navigate(AppDestination.ChatAI.route) },
                onNavigateToSettings = { navController.navigate(AppDestination.Settings.route) },
                onLogout = {
                    userViewModel.logout()
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Import.route) {
            val stockViewModel: StockViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            ImportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        @Suppress("DEPRECATION")
        composable(AppDestination.Export.route) {
            val stockViewModel: StockViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            ExportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Inventory.route) {
            InventoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.ImportHistory.route) {
            val viewModel: ImportHistoryViewModel = hiltViewModel()
            ImportHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.ExportHistory.route) {
            val viewModel: ExportHistoryViewModel = hiltViewModel()
            ExportHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.ChatAI.route) {
            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatAIScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Settings.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            SettingsScreen(
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
