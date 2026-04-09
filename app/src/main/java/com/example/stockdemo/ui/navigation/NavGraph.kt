package com.example.stockdemo.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stockdemo.ui.screens.chat.ChatAIScreen
import com.example.stockdemo.ui.screens.chat.ChatViewModel
import com.example.stockdemo.ui.screens.login.LoginScreen
import com.example.stockdemo.ui.screens.login.LoginViewModel
import com.example.stockdemo.ui.screens.login.MenuScreen
import com.example.stockdemo.ui.screens.login.UserViewModel
import com.example.stockdemo.ui.screens.stock.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                userViewModel = userViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Menu.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Menu.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            MenuScreen(
                userViewModel = userViewModel,
                onNavigateToImport = { navController.navigate(Screen.Import.route) },
                onNavigateToExport = { navController.navigate(Screen.Export.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToImportHistory = { navController.navigate(Screen.ImportHistory.route) },
                onNavigateToExportHistory = { navController.navigate(Screen.ExportHistory.route) },
                onNavigateToChatAI = { navController.navigate(Screen.ChatAI.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    userViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Import.route) {
            val stockViewModel: StockViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            ImportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        @Suppress("DEPRECATION")
        composable(Screen.Export.route) {
            val stockViewModel: StockViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            ExportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ImportHistory.route) {
            val viewModel: ImportHistoryViewModel = hiltViewModel()
            ImportHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ExportHistory.route) {
            val viewModel: ExportHistoryViewModel = hiltViewModel()
            ExportHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ChatAI.route) {
            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatAIScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) { PlaceholderScreen("Cài đặt") }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}
