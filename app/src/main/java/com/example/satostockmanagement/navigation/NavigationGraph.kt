package com.example.satostockmanagement.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.satostockmanagement.repository.StockRepository
import com.example.satostockmanagement.repository.UserRepository
import com.example.satostockmanagement.screens.*
import com.example.satostockmanagement.session.UserSession
import com.example.satostockmanagement.viewmodel.WarehouseViewModel
import com.example.satostockmanagement.viewmodel.stock.StockViewModel
import com.example.satostockmanagement.viewmodel.stock.StockViewModelFactory
import com.example.satostockmanagement.viewmodel.user.UserViewModel
import com.example.satostockmanagement.viewmodel.user.UserViewModelFactory

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Menu : Screen("menu")
    object Import : Screen("import")
    object Export : Screen("export")
    object Inventory : Screen("inventory")
    object Settings : Screen("settings")
}

@Composable
fun NavigationGraph(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController(),
    warehouseViewModel: WarehouseViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            repository = UserRepository(),
            userSession = UserSession(context)
        )
    )

    val stockViewModel: StockViewModel = viewModel(
        factory = StockViewModelFactory(
            repository = StockRepository()
        )
    )
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = userViewModel, // Pass viewModel vào đây
                onLoginSuccess = {
                    navController.navigate(Screen.Menu.route) {
                        // Xóa Login khỏi Backstack để người dùng không quay lại màn hình login được nữa
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Menu.route) {
            MenuScreen(
                userViewModel = userViewModel,
                onNavigateToImport = { navController.navigate(Screen.Import.route) },
                onNavigateToExport = { navController.navigate(Screen.Export.route) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onLogout = {
                    userViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        // Xóa toàn bộ stack để không thể nhấn Back quay lại Menu
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.Import.route) {
            ImportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Export.route) {
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

        composable(Screen.Settings.route) {
            SettingsScreen(
                settings = warehouseViewModel.settings.value,
                darkMode = darkMode,
                onDarkModeChange = onDarkModeChange,
                onSettingsChange = { newSettings ->
                    warehouseViewModel.updateSettings(newSettings)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}